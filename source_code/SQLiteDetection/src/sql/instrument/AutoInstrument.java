package sql.instrument;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import soot.Body;
import soot.BodyTransformer;
import soot.BooleanType;
import soot.ByteType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Transform;
import soot.Trap;
import soot.Unit;
import soot.dexpler.TrapMinimizer;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.JTrap;
import soot.options.Options;
import soot.toDex.TrapSplitter;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import sql.global.Main;
import sql.string.RegionNode;

public class AutoInstrument {

	private String [] args;
	private Map<String,RegionNode> sigToRegionNode;
	private Map<String,Set<String>> regionToDefine;
	private Set<String> targetEntries;
	private Set<String> targetMethods = new HashSet<>();
	private Map<Unit,Set<Trap>> unitToTrap = new HashMap<>();
	
	private boolean insertTimeAroundLoop = true;
	private boolean insertTimeAroundEntry = true;
	private boolean insertTransaction = false;
	private boolean insertRestart = false;
	private boolean insertTryCatch = false;
	private long insTime = 0;
	private int fieldIndex = 0;
	public AutoInstrument(String [] args,Map<String,RegionNode> sigToRegionNode,Map<String,Set<String>> regionToDefine, Set<String> entries)
	{
		this.args = args;
		this.sigToRegionNode = sigToRegionNode;
		this.regionToDefine = regionToDefine;
		targetEntries = entries;
		for(RegionNode rd:sigToRegionNode.values())
			targetMethods.add(rd.getCFG().getSignature());
	}
	
	public void performInstrument(String outputFile)
	{
		Options.v().set_output_format(Options.output_format_dex);
		
		Options.v().set_output_dir(args[2].substring(0, args[2].lastIndexOf("/"))+"/unopt");
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.yourInstrumenter", new BodyTransformer() {

			private Map<String,RegionNode> sigToRegionNode;
			private Map<String,Set<String>> regionToDefine;
			
			
			private BodyTransformer init(Map<String,RegionNode> sigToRegionNode,Map<String,Set<String>> regionToDefine){
				this.sigToRegionNode = sigToRegionNode;
				this.regionToDefine = regionToDefine;
				
				
		        return this;
		    }
			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) 
			{
				long start = System.currentTimeMillis();
				
				final PatchingChain<Unit> units = b.getUnits();
				if(insertTimeAroundEntry)
				{
					
					if(targetEntries.contains(b.getMethod().getSignature()))
					{
						boolean inserted =false;
						for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
							final Stmt s = (Stmt) iter.next();
							//first non-IdentityStmt
							if((!(s instanceof IdentityStmt))&&!inserted)
							{
								addTimeStampAfter(b, units, s,"Action");
								inserted = true;
								
							}
							if(s instanceof ReturnStmt || s instanceof ReturnVoidStmt || s instanceof ThrowStmt)
							{
								addTimeStampBefore(b, units, s,"Action");
							}
							
						}
					}
				}
				if(targetMethods.contains(b.getMethod().getSignature()))
				{
					if(insertTryCatch)
					{
						for(Trap trap: b.getTraps())
						{
							mapUnitToTrap(trap,units);

						}
					}
					//sort the region from outer to inner. The outer region has a larger region number except for the root node. 
					List<String> regionOrder = new ArrayList<>();
					List<String> rootRegion = new ArrayList<>();
					for(String region : regionToDefine.keySet())
					{
						
						CFGInterface cfg = sigToRegionNode.get(region).getCFG();
						if(b.getMethod().getSignature().equals(cfg.getSignature()))
						{
							int regionNum = Integer.parseInt(region.substring(region.lastIndexOf(">")+1));
							if(regionNum == 0)
								rootRegion.add(region);
							else
								regionOrder.add(region);
						}

					}
					Collections.sort(regionOrder, new Comparator<String>() {
						public int compare(String region1, String region2) {
							int regionNum1 = Integer.parseInt(region1.substring(region1.lastIndexOf(">")+1));
							int regionNum2 = Integer.parseInt(region2.substring(region2.lastIndexOf(">")+1));
							return regionNum2 - regionNum1; 
															
						}
					});
					rootRegion.addAll(regionOrder);
					//System.out.println("Order:"+rootRegion);
					for(String region: rootRegion)
					{
						
						CFGInterface cfg = sigToRegionNode.get(region).getCFG();

											
						for(String define:regionToDefine.get(region))
						{
							if(define.startsWith("A@"))
							{
								NodeInterface defineNode = cfg.getNodeFromOffset(Integer.parseInt(define.replaceAll("A@", "")));
								Unit u = (Unit) ((Node)defineNode).getActualNode();
								Local db = (Local) u.getDefBoxes().get(0).getValue();
								instrumentUsingLocalOnRegion(sigToRegionNode.get(region),b,db);
							}
							else if(define.startsWith("B@"))
							{
								
			
						        

						        int i1 = define.lastIndexOf(" ");
						        
						        String fieldName = define.substring(i1+1,define.length()-1);
						        
						        //db = r0.<fieldName>		
						        Local r0 = (Local) ((Unit)((Node)cfg.getNodeFromOffset(0)).getActualNode()).getDefBoxes().get(0).getValue();
						        
						        Local db = Jimple.v().newLocal("db", RefType.v("android.database.sqlite.SQLiteDatabase"));
						        b.getLocals().add(db);
						        SootClass sc = b.getMethod().getDeclaringClass();
						        
						        SootField f = sc.getField(fieldName,RefType.v("android.database.sqlite.SQLiteDatabase"));
						        
					
						        Unit ini = null;
						        
						        if(f.getDeclaration().contains(" static "))
						        	ini = Jimple.v().newAssignStmt(db, Jimple.v().newStaticFieldRef(f.makeRef())); 
						        else
						        	ini = Jimple.v().newAssignStmt(db, Jimple.v().newInstanceFieldRef(r0, f.makeRef()));
						        
						        	
						        
						        //add db = r0.field
								instrumentFieldOnRegion(sigToRegionNode.get(region),b,db,ini,fieldName);
								
							}
							
							
						}
						
						
					}
					
					b.validate();
					
				}
						
				long end = System.currentTimeMillis();
				
				insTime+= end-start;
			
			}
			

			

		}.init(sigToRegionNode,regionToDefine)
		));
		

		soot.Main.main(args);
		
		Main.rewriteTotal = insTime;
/*	    try {
				PrintWriter pwTime = new PrintWriter(new FileWriter("/home/yingjun/Documents/sqlite/time.txt", true));
				pwTime.println("InsTotal:"+insTime);
				pwTime.close();
	    }
		catch(IOException ex)
		{
		
		}*/

	}
	private void mapUnitToTrap(Trap trap,PatchingChain<Unit> units) {
		Unit begin = trap.getBeginUnit();
		Unit end = trap.getEndUnit();
		Unit current = begin;
		while(current!=end)
		{
			if(!unitToTrap.containsKey(current))
				unitToTrap.put(current, new HashSet<Trap>());
			unitToTrap.get(current).add(trap);
			current = units.getSuccOf(current);
		}
		if(!unitToTrap.containsKey(current))
			unitToTrap.put(current, new HashSet<Trap>());
		unitToTrap.get(current).add(trap);
		
	}

	private void insertRestartTransaction(RegionNode regionNode,Body b, Local $start, Local db, long interval) {
				
		/*
		$l3 = staticinvoke <java.lang.System: long currentTimeMillis()>()
		$l4 = $l3 - $l0->[]
		$b5 = $l4 cmp 10000L->[]
		if $b5 <= 0 goto virtualinvoke r1.<android.database.sqlite.SQLiteDatabase: android.database.sqlite.SQLiteStatement compileStatement(java.lang.String)>("abc")
		virtualinvoke r1.<android.database.sqlite.SQLiteDatabase: void setTransactionSuccessful()>()
		virtualinvoke r1.<android.database.sqlite.SQLiteDatabase: void endTransaction()>()
		virtualinvoke r1.<android.database.sqlite.SQLiteDatabase: void beginTransaction()>()
		$l0 = staticinvoke <java.lang.System: long currentTimeMillis()>()
		*/
		
		//Unit beginTry = (Unit) ((Node)regionNode.getBeginTryNode()).getActualNode();
		Unit loopSource = (Unit) ((Node)regionNode.getBackEdge().getSource()).getActualNode();
		if(loopSource instanceof IfStmt)
			loopSource = b.getUnits().getPredOf(loopSource);
		Local $end = Jimple.v().newLocal("$end",LongType.v());
		Local $dif = Jimple.v().newLocal("$dif",LongType.v());
		Local $comp = Jimple.v().newLocal("$comp",ByteType.v());
		b.getLocals().add($end);
		b.getLocals().add($dif);
		b.getLocals().add($comp);
        SootMethod time = Scene.v().getSootClass("java.lang.System").getMethod("long currentTimeMillis()");		        
        List<Unit> restart = new ArrayList<>();
        restart.add(Jimple.v().newAssignStmt($end,Jimple.v().newStaticInvokeExpr(time.makeRef())));
        restart.add(Jimple.v().newAssignStmt($dif, Jimple.v().newSubExpr($end, $start)));
        restart.add(Jimple.v().newAssignStmt($comp, Jimple.v().newCmpExpr($dif, LongConstant.v(interval))));
        Unit ifstmt = Jimple.v().newIfStmt(Jimple.v().newLeExpr($comp,IntConstant.v(0)), loopSource);
        restart.add(ifstmt);       
		SootMethod toCallSuc = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void setTransactionSuccessful()");
		restart.add(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallSuc.makeRef())));
		SootMethod toCallEnd = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void endTransaction()");
		restart.add(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallEnd.makeRef())));
		SootMethod toCallBegin = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void beginTransaction()");
		restart.add(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallBegin.makeRef())));
		
		restart.add(Jimple.v().newAssignStmt($start,Jimple.v().newStaticInvokeExpr(time.makeRef())));
		b.getUnits().insertBefore(restart, loopSource);
		((IfStmt)ifstmt).setTarget(loopSource);
		
		Node<Unit> insertedBeginTryNode = new Node<Unit>();
		insertedBeginTryNode.setActualNode(restart.get(0));
		regionNode.setInsertedBeginTryNode(insertedBeginTryNode);
	
	}
	int tryNum = 0;
	private void insertTryCatch(RegionNode regionNode,Body b, Local db, Unit begin,List<Unit> end) {
		PatchingChain<Unit> units = b.getUnits();
		
		
		Unit source = (Unit) ((Node)regionNode.getBackEdge().getSource()).getActualNode();
		Unit dest = (Unit) ((Node)regionNode.getBackEdge().getDestination()).getActualNode();
		SootMethod toCallEnd = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void endTransaction()");
		SootMethod toCallSuc = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void setTransactionSuccessful()");
		SootMethod toCallCheck = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("boolean inTransaction()");
		//add an endTransaction to the existing handler
		if(unitToTrap.containsKey(dest))
		{	
			//add a dbexp_index = null to the method entry
			Local dbexp = Jimple.v().newLocal("dbexp"+fieldIndex, RefType.v("android.database.sqlite.SQLiteDatabase"));
	        b.getLocals().add(dbexp);
			fieldIndex++;
	        Unit first = units.getFirst();
	        while(first instanceof IdentityStmt)
	        	first = units.getSuccOf(first);
	        units.insertBefore(Jimple.v().newAssignStmt(dbexp, NullConstant.v()), first);
	        	        
			//add a dbexp1 = db after db.beginTransaction
			units.insertAfter(Jimple.v().newAssignStmt(dbexp, db), begin);
			//add a dbexp1 = null after db.endTransaction
			for(Unit e : end)
				units.insertAfter(Jimple.v().newAssignStmt(dbexp, NullConstant.v()), e);
			//add if(dbexp1!=null) dbexp1.endTransaction to every existing handler
			
			/*
			if(fieldName==null)
				System.err.println("Field name for local variable is not specified.");
	        //dbexp = r0.<fieldName>		
	        Local r0 = (Local) ((Unit)((Node)regionNode.getCFG().getNodeFromOffset(0)).getActualNode()).getDefBoxes().get(0).getValue();	        
	        Local dbexp = Jimple.v().newLocal("dbexp", RefType.v("android.database.sqlite.SQLiteDatabase"));
	        b.getLocals().add(dbexp);
	        SootClass sc = b.getMethod().getDeclaringClass();	        
	        SootField f = sc.getField(fieldName,RefType.v("android.database.sqlite.SQLiteDatabase"));	        
	        Unit ini = null;	        
	        if(f.getDeclaration().contains(" static "))
	        	ini = Jimple.v().newAssignStmt(dbexp, Jimple.v().newStaticFieldRef(f.makeRef())); 
	        else
	        	ini = Jimple.v().newAssignStmt(dbexp, Jimple.v().newInstanceFieldRef(r0, f.makeRef()));
	        
	        */
			
			Set<Unit> tryEnds = new HashSet<>();
			//add to finally block
			for(Trap trap: unitToTrap.get(dest))
			{
				if(!trap.getEndUnit().equals(trap.getHandlerUnit()))
					tryEnds.add(trap.getEndUnit());
				Unit handlerBegin = trap.getHandlerUnit();
				Unit handlerBeginNext = units.getSuccOf(handlerBegin);
				
				//$z0 = virtualinvoke r1.<android.database.sqlite.SQLiteDatabase: boolean inTransaction()>()->[]
				
				//if dbexp == null goto handlerBeginNext
				//virtualinvoke dbexp.<android.database.sqlite.SQLiteDatabase: void setTransactionSuccessful()>()->[]
				//virtualinvoke dbexp.<android.database.sqlite.SQLiteDatabase: void endTransaction()>()->[]
				List<Unit> insertToHandler = new ArrayList<>();						
				IfStmt ifcheck = Jimple.v().newIfStmt(Jimple.v().newEqExpr(dbexp, NullConstant.v()), handlerBeginNext);
				insertToHandler.add(ifcheck);
				insertToHandler.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(dbexp, toCallSuc.makeRef())));
				insertToHandler.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(dbexp, toCallEnd.makeRef())));
				units.insertAfter(insertToHandler, handlerBegin);
				ifcheck.setTarget(handlerBeginNext);				
			}
			/*
			for(Unit tryEnd : tryEnds)
			{
				List<Unit> insertToTryEnd = new ArrayList<>();						
				IfStmt ifcheck = Jimple.v().newIfStmt(Jimple.v().newEqExpr(dbexp, NullConstant.v()), tryEnd);
				insertToTryEnd.add(ifcheck);
				insertToTryEnd.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(dbexp, toCallSuc.makeRef())));
				insertToTryEnd.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(dbexp, toCallEnd.makeRef())));
				units.insertBefore(insertToTryEnd, tryEnd);
				ifcheck.setTarget(tryEnd);
			}
			*/
		}
		else
		{
			
			//insert a handler
			//0. goto the original next statement before insert
			//1. $exp := @caughtexception
			//2. thr = $exp
			//3. setTransactionSuccessful
			//4. endTransaction
			//5. throw thr
			Local exp = Jimple.v().newLocal("$exp"+tryNum, RefType.v("java.lang.Throwable"));
			tryNum++;
			b.getLocals().add(exp);
			List<Unit> handler = new ArrayList<>();
			
			Stmt go = Jimple.v().newGotoStmt(units.getSuccOf(source));
			
			if(units.getSuccOf(source)!=null&&!units.getSuccOf(source).toString().contains("@caughtexception"))
				handler.add(go);
	
			Stmt caught = Jimple.v().newIdentityStmt(exp, Jimple.v().newCaughtExceptionRef());
			handler.add(caught);
			handler.add(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallSuc.makeRef())));
			handler.add(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallEnd.makeRef())));
			Stmt thr = Jimple.v().newThrowStmt(exp);
			handler.add(thr);
			
			
			
			/*
			Unit be = null;
			if(regionNode.getInsertedBeginTryNode()!=null)
				be = (Unit) ((Node)regionNode.getInsertedBeginTryNode()).getActualNode();
			else
				be = (Unit) ((Node)regionNode.getBeginTryNode()).getActualNode();
			Unit en = null;
			for(Unit temp: units)
			{
				if(temp.toString().equals("return")&&units.getPredOf(temp).toString().contains("close"))
					en = temp;
			}
			*/
			//add handler units to finally block
			units.insertAfter(handler, source);
			
			go.redirectJumpsToThisTo(null);
			//b.getTraps().clear();
			
			Unit current = dest;
			Unit beginTrap = dest;
			while(current != caught)
			{
	
				if(current instanceof ReturnStmt || current instanceof ReturnVoidStmt)
				{
					
					Trap trap = new JTrap(Scene.v().getSootClass("java.lang.Throwable"),beginTrap,current,caught);
					b.getTraps().add(trap);
					mapUnitToTrap(trap, units);
					beginTrap = units.getSuccOf(current);
				}
				current = units.getSuccOf(current);
			}
			
			//beginTrap = units.getSuccOf(beginTrap);
			//beginTrap = units.getSuccOf(beginTrap);
			//beginTrap = units.getSuccOf(beginTrap);
			Trap trap = new JTrap(Scene.v().getSootClass("java.lang.Throwable"),beginTrap,current,caught);
			
			//if(!beginTrap.toString().equals("$z2 = virtualinvoke $r4.<java.util.StringTokenizer: boolean hasMoreTokens()>()"))
			b.getTraps().add(trap);
			mapUnitToTrap(trap, units);
			//TrapMinimizer.v().transform(b);
			//TrapSplitter.v().transform(b);
		}
		/*
		for(Trap t: b.getTraps())
		{
			System.out.println(t);
			System.out.println(units.getPredOf(t.getBeginUnit()));
		}
		*/
		
	}
	
	private void instrumentFieldOnRegion(RegionNode regionNode,Body b, Local db, Unit ini, String fieldName) {
		
		PatchingChain<Unit> units = b.getUnits();
		
		//System.out.println(regionNode.getCFG().getSignature());
		//System.out.println(regionNode.getCFGToDot());
		//for(NodeInterface n: regionNode.getCFG().getAllNodes())
		//{
		//	System.out.println(n.getOffset()+" "+((Node)n).getActualNode());
		//}
		
		
		//System.out.println("Backedge:"+ regionNode.getBackEdge().getSource().getOffset()+"->"+regionNode.getBackEdge().getDestination().getOffset());
		NodeInterface p=regionNode.getBeginNode();
		
		
		if(p!=null)
		{
			//System.out.println("Begin"+p.getOffset());
			Unit start = (Unit) ((Node)p).getActualNode();
			Unit beginT = null;
			List<Unit> endT = new ArrayList<>();
			
			if(insertTransaction)
			{
				//insert transaction restarting
				if(insertRestart)
				{
					//$start = staticinvoke <java.lang.System: long currentTimeMillis()>()
					Local $start = Jimple.v().newLocal("$start",LongType.v());
					b.getLocals().add($start);
			        SootMethod time = Scene.v().getSootClass("java.lang.System").getMethod("long currentTimeMillis()");		        
			        units.insertAfter(Jimple.v().newAssignStmt($start,Jimple.v().newStaticInvokeExpr(time.makeRef())), start);
			        insertRestartTransaction(regionNode, b, $start, db, 500L);
				}
			
				//insert begin
				SootMethod toCallBegin = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void beginTransaction()");
			
				beginT = Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallBegin.makeRef()));
				units.insertAfter(beginT, start);
				
				units.insertAfter(ini, start);
			}
			
			if(insertTimeAroundLoop)
			{
				addTimeStampAfter(b, b.getUnits(), start,"Loop");
				//add String "itr@region number" to after the entry of the loop
				//Unit entry = (Unit) ((Node)regionNode.getBackEdge().getDestination()).getActualNode();
				//addItrAfter(b, b.getUnits(),entry,""+regionNode.getRegionNumber());
			}

		
			for(NodeInterface c : regionNode.getEndNode())
			{
				//System.out.println("End"+c.getOffset());
				
				Unit end = (Unit) ((Node)c).getActualNode();
				
				//insert end 
				if(insertTransaction)
				{
				SootMethod toCallSuc = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void setTransactionSuccessful()");
				units.insertBefore(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallSuc.makeRef())), end);
				
				
				SootMethod toCallEnd = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void endTransaction()");
				Stmt endTransaction = Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallEnd.makeRef()));
				units.insertBefore(endTransaction, end);
				
				endT.add(endTransaction);

				}
				if(insertTimeAroundLoop)
					addTimeStampBefore(b, units, end,"Loop");
			}
			if(insertTransaction&&insertTryCatch)
				insertTryCatch(regionNode,b,db,beginT,endT);
			//for(Unit temp: units)
			//	System.out.println(temp+ "-> "+temp.getBoxesPointingToThis());

		}
	}


	private void instrumentUsingLocalOnRegion(RegionNode regionNode,Body b, Local db) {
		
		PatchingChain<Unit> units = b.getUnits();
		//System.out.println(regionNode.getCFG().getSignature());
		//System.out.println(regionNode.getCFGToDot());
		//for(NodeInterface n: regionNode.getCFG().getAllNodes())
		//{
		//	System.out.println(n.getOffset()+" "+((Node)n).getActualNode());
		//}
		//System.out.println("Backedge:"+ regionNode.getBackEdge().getSource().getOffset()+"->"+regionNode.getBackEdge().getDestination().getOffset());

		NodeInterface p =  regionNode.getBeginNode();
		


		if(p!=null)
		{
			//System.out.println("Begin"+p.getOffset());
			Unit start = (Unit) ((Node)p).getActualNode();
			Unit beginT = null;
			List<Unit> endT = new ArrayList<>();
			//insert begin				
			if(insertTransaction)
			{
				//insert transaction restarting
				if(insertRestart)
				{
					//$start = staticinvoke <java.lang.System: long currentTimeMillis()>()
					Local $start = Jimple.v().newLocal("$start",LongType.v());
					b.getLocals().add($start);
			        SootMethod time = Scene.v().getSootClass("java.lang.System").getMethod("long currentTimeMillis()");		        
			        units.insertAfter(Jimple.v().newAssignStmt($start,Jimple.v().newStaticInvokeExpr(time.makeRef())), start);
			        insertRestartTransaction(regionNode, b, $start, db, 500L);
				}
				
				SootMethod toCallBegin = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void beginTransaction()");
				beginT = Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallBegin.makeRef()));
				units.insertAfter(beginT, start);
				
			}
			
			if(insertTimeAroundLoop)
				addTimeStampAfter(b, b.getUnits(), start,"Loop");
		
			for(NodeInterface c :regionNode.getEndNode())
			{
				//System.out.println("End"+c.getOffset());
				Unit end = (Unit) ((Node)c).getActualNode();
				
				//insert end 
				if(insertTransaction)
				{
				SootMethod toCallSuc = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void setTransactionSuccessful()");
				units.insertBefore(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallSuc.makeRef())), end);
				
				
				SootMethod toCallEnd = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void endTransaction()");
				
				
				Unit endTransaction = Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallEnd.makeRef()));
				units.insertBefore(endTransaction, end);
				endT.add(endTransaction);
				}
				if(insertTimeAroundLoop)
				addTimeStampBefore(b, units, end,"Loop");
			}
			if(insertTransaction&&insertTryCatch)
			{
				
				insertTryCatch(regionNode,b,db,beginT,endT);
			}
			//for(Unit temp: units)
			//	System.out.println(temp+ "-> "+temp.getBoxesPointingToThis());
		}
		
	}
	
	private void addItrAfter(Body body,PatchingChain<Unit> units,Unit start,String regionNum)
	{
    	List<Unit> list = new ArrayList<>();
	 	
		// insert "$r4 = java.lang.System.out;"   $r4 -> tmp4
        Local $r4 = Jimple.v().newLocal("itr4", RefType.v("java.io.PrintStream"));
        body.getLocals().add($r4);

        list.add(Jimple.v().newAssignStmt($r4, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())));
       
        // insert "$r3 = new java.lang.StringBuilder" $r3 -> tmp3
        Local $r3 = Jimple.v().newLocal("itr3", RefType.v("java.lang.StringBuilder"));
        body.getLocals().add($r3);

	        
        list.add(Jimple.v().newAssignStmt($r3, Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder"))));
        
       // insert " specialinvoke $r3.<java.lang.StringBuilder: void <init>(java.lang.String)>("Start") "
        
        SootMethod init = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("void <init>(java.lang.String)");
       
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr($r3, init.makeRef(), StringConstant.v("Itr@"+regionNum))));
        
       //insert "$l1 = staticinvoke <java.lang.System: long currentTimeMillis()>()"   $l1 -> tmp1
        Local $l1 = Jimple.v().newLocal("itr1", LongType.v());
        body.getLocals().add($l1);
        
        SootMethod time = Scene.v().getSootClass("java.lang.System").getMethod("long currentTimeMillis()");

        
        list.add(Jimple.v().newAssignStmt($l1,Jimple.v().newStaticInvokeExpr(time.makeRef())));
        

        //insert "$r5 = virtualinvoke $r3.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>($l1)"  $r5 -> tmp5
        Local $r5 = Jimple.v().newLocal("itr5", RefType.v("java.lang.StringBuilder"));
        body.getLocals().add($r5);
        SootMethod append = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.StringBuilder append(long)");

        
        list.add(Jimple.v().newAssignStmt($r5,Jimple.v().newVirtualInvokeExpr($r3, append.makeRef(), $l1)));
        
       //insert $r6 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.String toString()>()   $r6 -> tmp6
        Local $r6 = Jimple.v().newLocal("itr6",RefType.v("java.lang.String"));
        body.getLocals().add($r6);
        SootMethod toString = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.String toString()");
        
        list.add(Jimple.v().newAssignStmt($r6, Jimple.v().newVirtualInvokeExpr($r5, toString.makeRef())));
        
        //insert virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>($r6)
        SootMethod println = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");
        
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr($r4, println.makeRef(), $r6)));
        
        
        
        units.insertAfter(list, start);
	}
	
    private void addTimeStampAfter(Body body,PatchingChain<Unit> units,Unit start,String act)
    {
    	List<Unit> list = new ArrayList<>();
	 	
		// insert "$r4 = java.lang.System.out;"   $r4 -> tmp4
        Local $r4 = Jimple.v().newLocal("start4", RefType.v("java.io.PrintStream"));
        body.getLocals().add($r4);

        list.add(Jimple.v().newAssignStmt($r4, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())));
       
        // insert "$r3 = new java.lang.StringBuilder" $r3 -> tmp3
        Local $r3 = Jimple.v().newLocal("start3", RefType.v("java.lang.StringBuilder"));
        body.getLocals().add($r3);

	        
        list.add(Jimple.v().newAssignStmt($r3, Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder"))));
        
       // insert " specialinvoke $r3.<java.lang.StringBuilder: void <init>(java.lang.String)>("Start") "
        
        SootMethod init = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("void <init>(java.lang.String)");
       
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr($r3, init.makeRef(), StringConstant.v("Start"+act))));
        
       //insert "$l1 = staticinvoke <java.lang.System: long currentTimeMillis()>()"   $l1 -> tmp1
        Local $l1 = Jimple.v().newLocal("start1", LongType.v());
        body.getLocals().add($l1);
        
        SootMethod time = Scene.v().getSootClass("java.lang.System").getMethod("long currentTimeMillis()");

        
        list.add(Jimple.v().newAssignStmt($l1,Jimple.v().newStaticInvokeExpr(time.makeRef())));
        

        //insert "$r5 = virtualinvoke $r3.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>($l1)"  $r5 -> tmp5
        Local $r5 = Jimple.v().newLocal("start5", RefType.v("java.lang.StringBuilder"));
        body.getLocals().add($r5);
        SootMethod append = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.StringBuilder append(long)");

        
        list.add(Jimple.v().newAssignStmt($r5,Jimple.v().newVirtualInvokeExpr($r3, append.makeRef(), $l1)));
        
       //insert $r6 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.String toString()>()   $r6 -> tmp6
        Local $r6 = Jimple.v().newLocal("start6",RefType.v("java.lang.String"));
        body.getLocals().add($r6);
        SootMethod toString = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.String toString()");
        
        list.add(Jimple.v().newAssignStmt($r6, Jimple.v().newVirtualInvokeExpr($r5, toString.makeRef())));
        
        //insert virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>($r6)
        SootMethod println = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");
        
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr($r4, println.makeRef(), $r6)));
        
        
        
        units.insertAfter(list, start);
    }
    private void addTimeStampBefore(Body body,PatchingChain<Unit> units,Unit end,String act)
    {
    	
    	List<Unit> list = new ArrayList<>();
    	 	
		// insert "$r4 = java.lang.System.out;"   $r4 -> tmp4
        Local $r4 = Jimple.v().newLocal("end4", RefType.v("java.io.PrintStream"));
        body.getLocals().add($r4);

        list.add(Jimple.v().newAssignStmt($r4, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())));
       
        // insert "$r3 = new java.lang.StringBuilder" $r3 -> tmp3
        Local $r3 = Jimple.v().newLocal("end3", RefType.v("java.lang.StringBuilder"));
        body.getLocals().add($r3);

	        
        list.add(Jimple.v().newAssignStmt($r3, Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder"))));
        
       // insert " specialinvoke $r3.<java.lang.StringBuilder: void <init>(java.lang.String)>("Start") "
        
        SootMethod init = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("void <init>(java.lang.String)");
       
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr($r3, init.makeRef(), StringConstant.v("End"+act))));
        
       //insert "$l1 = staticinvoke <java.lang.System: long currentTimeMillis()>()"   $l1 -> tmp1
        Local $l1 = Jimple.v().newLocal("end1", LongType.v());
        body.getLocals().add($l1);
        
        SootMethod time = Scene.v().getSootClass("java.lang.System").getMethod("long currentTimeMillis()");

        
        list.add(Jimple.v().newAssignStmt($l1,Jimple.v().newStaticInvokeExpr(time.makeRef())));
        

        //insert "$r5 = virtualinvoke $r3.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>($l1)"  $r5 -> tmp5
        Local $r5 = Jimple.v().newLocal("end5", RefType.v("java.lang.StringBuilder"));
        body.getLocals().add($r5);
        SootMethod append = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.StringBuilder append(long)");

        
        list.add(Jimple.v().newAssignStmt($r5,Jimple.v().newVirtualInvokeExpr($r3, append.makeRef(), $l1)));
        
       //insert $r6 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.String toString()>()   $r6 -> tmp6
        Local $r6 = Jimple.v().newLocal("end6",RefType.v("java.lang.String"));
        body.getLocals().add($r6);
        SootMethod toString = Scene.v().getSootClass("java.lang.StringBuilder").getMethod("java.lang.String toString()");
        
        list.add(Jimple.v().newAssignStmt($r6, Jimple.v().newVirtualInvokeExpr($r5, toString.makeRef())));
        
        //insert virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>($r6)
        SootMethod println = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");
        
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr($r4, println.makeRef(), $r6)));
        
        
        
        units.insertBefore(list, end);
        
    }
    private static void addFieldToClass(String className,String fieldName)
    {
    	SootClass sc=Scene.v().loadClassAndSupport(className);
    	
    	//add a field
    	SootField id = new SootField(fieldName,RefType.v("android.database.sqlite.SQLiteDatabase"),Modifier.PRIVATE);
    	sc.addField(id);
    
    	//add initialization to the constructor
    
    }

}

