package sql.instrument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import edu.usc.sql.graphs.cfg.SootCFG;
import soot.Body;
import soot.BodyTransformer;
import soot.Modifier;
import soot.SceneTransformer;
import soot.Local;
import soot.LongType;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocalBox;
import soot.options.Options;
import soot.util.Chain;


public class AndroidInstrument {
	
	public static void main(String[] args) {
		
		//prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		/*
		String jrepath = "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar";
		String appPath = "/home/yingjun/Documents/sqlite/Android/App183/classes";
        Options.v().set_soot_classpath(jrepath+":"+appPath);
      	Options.v().set_whole_program(true);
		Options.v().set_src_prec(Options.src_prec_J);
		Options.v().set_android_jars("/home/yingjun/Documents/sqlite/sqlAndroid");
      	List<String> stringlist=new LinkedList<String>();
      	stringlist.add(appPath);   	
      	Options.v().set_process_dir(stringlist);
      	Options.v().set_android_jars("");
      	*/
		//output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);

		Options.v().set_allow_phantom_refs(true);

        // resolve the PrintStream and System soot-classes
		Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
        Scene.v().addBasicClass("android.database.sqlite.SQLiteDatabase",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.StringBuilder",SootClass.SIGNATURES);
        
        //App183
        
   		final String targetMethod = "markCategories";
        final int startID = 31,endID = 34;
        final int cloneID = 4;
        final boolean isEncapsulated = false;
        final String listenerSig = "<com.a1.quiz.asvab.free.ChooseActivity: void start(android.view.View)>";
        
/*        //App222
        final String targetMethod = "addDecorations";
        final int startID = 4,endID = 7,cloneID=-100;
        final boolean isEncapsulated = false;
        final String listenerSig = "<com.abto.manicure.actionhandlers.DecorateHandler$3: void onClick(android.view.View)>";
        */
/*        //App275
        final String targetMethod = "delete_all";
        final int startID = 3,endID = 7;
        final int cloneID = 1;
        final boolean isEncapsulated = true;
         final String listenerSig = "<com.adt.barnqr.SavedData$7: void onClick(android.content.DialogInterface,int)>"
        */
        
        
        final boolean inTransaction = true;

        final boolean vLens = true;
        
/*        PackManager.v().getPack("wjtp").add(
        	      new Transform("wjtp.myTransform", new SceneTransformer() {
        	        protected void internalTransform(String phaseName,
        	            Map options) {
        	          
        	          
        	          addMethodToClass("com.dbseperate.DBSeperateClass","mDb","beginTransaction");
        	          addMethodToClass("com.dbseperate.DBSeperateClass","mDb","endTransaction");
        	          addMethodToClass("com.dbseperate.DBSeperateClass","mDb","setTransactionSuccessful");
        	        }
        	      }));*/
        


		
        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

        	
			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
				final PatchingChain<Unit> units = b.getUnits();
				

			
			  //add timestamp at the entry and exit of the method
			   
			  if(vLens == true)
				{
					
				  	if(b.getMethod().getSignature().equals(listenerSig))
				  	{
						boolean inserted =false;
						for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
							final Stmt s = (Stmt) iter.next();
							//first non-IdentityStmt
							if((!(s instanceof IdentityStmt))&&!inserted)
							{
								addTimeStampAfter(b, units, s);
								inserted = true;
								
							}
							if(s instanceof ReturnStmt || s instanceof ReturnVoidStmt || s instanceof ThrowStmt)
							{
								addTimeStampBefore(b, units, s);
							}
							
						}
				  	}
				

				
				}
				
				
				//if(b.getMethod().getSignature().contains("com.abto.manicure.dao.DatabaseHelper"))
				
				if(!b.getMethod().getSignature().contains(targetMethod))
						return;
				System.out.println(b.getMethod().getSignature());
				
				Unit start=null,end=null,clone=null;
				
				CFGInterface cfg = new SootCFG(b.getMethod().getSignature(),b.getMethod());
				System.out.println(cfg.toDot());
				for(NodeInterface n:cfg.getAllNodes())
				{
					System.out.println(n.getOffset().toString()+" "+((Node)n).getActualNode());
					if(n.getOffset()==startID)
						start = (Unit) ((Node)n).getActualNode();
					else if(n.getOffset()== endID)
						end = (Unit) ((Node)n).getActualNode();
					else if(n.getOffset()==cloneID)
						clone = (Unit) ((Unit) ((Node)n).getActualNode()).clone();
					
				}
				

				
				if(inTransaction)
				{
					Local db = null;
					
					
					//case this.db
					if(clone!=null)
					{
						db = (Local) clone.getDefBoxes().get(0).getValue();
					}
					//case db is a local var
					else
					{	
						for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
							final Unit u = iter.next();
							
							if(u instanceof Stmt)
							{					
								if(((Stmt)u).containsInvokeExpr())
								{
									String sig = ((Stmt)u).getInvokeExpr().getMethod().getSignature();
									if(sig.equals("<android.database.sqlite.SQLiteDatabase: long insert(java.lang.String,java.lang.String,android.content.ContentValues)>"))
									{
										System.out.println("Got You"+u.getUseBoxes());
										
										
										for(ValueBox v:u.getUseBoxes())
											if(v instanceof JimpleLocalBox)
												db = (Local) v.getValue();
										
									}
								}
							}
						}
					}
			
					
					
					if(start==null||end==null||db==null)
					{
						System.out.println("NOT FOUND!!!");
					}
					else
					{
						
					String className;
					if(isEncapsulated)
						className = "com.dbseperate.DBSeperateClass";
					else
						className = "android.database.sqlite.SQLiteDatabase";
						
					//insert begin				
					SootMethod toCallBegin = Scene.v().getSootClass(className).getMethod("void beginTransaction()");
					units.insertAfter(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallBegin.makeRef())), start);
					
					if(clone!=null)
					units.insertAfter((Unit) clone.clone(), start);
	
					
				
					//insert set
					if(clone!=null)
					units.insertBefore((Unit) clone.clone(), end);
					
					SootMethod toCallSuc = Scene.v().getSootClass(className).getMethod("void setTransactionSuccessful()");
					units.insertBefore(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallSuc.makeRef())), end);
					
					//insert end
					if(clone!=null)
					units.insertBefore((Unit) clone.clone(), end);
					
					SootMethod toCallEnd = Scene.v().getSootClass(className).getMethod("void endTransaction()");
					units.insertBefore(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallEnd.makeRef())), end);
					}
					
				}
				
				addTimeStampAroundLoop(b, units, start,end);
				//insert time stamp
				//addAgentTimeStamp(b, units,start, end);
				b.validate();
				
				
				
				CFGInterface cfgNew = new SootCFG(b.getMethod().getSignature(),b.getMethod());
				System.out.println(cfgNew.toDot());
				for(NodeInterface n:cfgNew.getAllNodes())
				{
					
					System.out.println(n.getOffset().toString()+" "+((Node)n).getActualNode());
				}
			}


		}));
		

        
		soot.Main.main(args);
	}

    private static Local addTmpRef(Body body)
    {
        Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
        body.getLocals().add(tmpRef);
        return tmpRef;
    }
    
    private static Local addTmpString(Body body)
    {
        Local tmpString = Jimple.v().newLocal("tmpString", RefType.v("java.lang.String")); 
        body.getLocals().add(tmpString);
        return tmpString;
    }
    
    
    private static void addAgentTimeStamp(Body body,PatchingChain<Unit> units,Unit start,Unit end)
    {
    	
    	SootMethod startTime = Scene.v().getSootClass("usc.edu.AgentURLConnection").getMethod("void LogCallStart()");
    	Stmt startInvoke = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(startTime.makeRef()));
    	units.insertAfter(startInvoke, start);
    	
    	SootMethod endTime = Scene.v().getSootClass("usc.edu.AgentURLConnection").getMethod("void LogCallReturn()");
    	Stmt endInvoke = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(endTime.makeRef()));
        units.insertBefore(endInvoke, end);
    	 
    }
    private static void addSimpleTimeStamp(Body body,PatchingChain<Unit> units,Unit end)
    {
    	List<Unit> list = new ArrayList<>();
		// insert "$r4 = java.lang.System.out;"   $r4 -> tmp4
        Local $r4 = Jimple.v().newLocal("end4", RefType.v("java.io.PrintStream"));
        body.getLocals().add($r4);

        list.add(Jimple.v().newAssignStmt($r4, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())));
       
       
        //insert "$l1 = staticinvoke <java.lang.System: long currentTimeMillis()>()"   $l1 -> tmp1
        Local $l1 = Jimple.v().newLocal("end1", LongType.v());
        body.getLocals().add($l1);
        SootMethod time = Scene.v().getSootClass("java.lang.System").getMethod("long currentTimeMillis()");

        
        list.add(Jimple.v().newAssignStmt($l1,Jimple.v().newStaticInvokeExpr(time.makeRef())));
        
        
        
        SootMethod println = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(long)");     
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr($r4, println.makeRef(),$l1)));
        
        units.insertBefore(list, end);
    }
    
    static void addTimeStampAfter(Body body,PatchingChain<Unit> units,Unit start)
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
       
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr($r3, init.makeRef(), StringConstant.v("Start"))));
        
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
    static void addTimeStampBefore(Body body,PatchingChain<Unit> units,Unit end)
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
       
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr($r3, init.makeRef(), StringConstant.v("End"))));
        
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
    
    static void addTimeStampAroundLoop(Body body,PatchingChain<Unit> units,Unit start,Unit end)
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
       
        list.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr($r3, init.makeRef(), StringConstant.v("End"))));
        
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
        
        
        
    	List<Unit> startList = new ArrayList<>(); 	
    	for(int i=0;i<list.size();i++)
    	{
    		if(i==2)
    			startList.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr($r3, init.makeRef(), StringConstant.v("Start"))));
    		else 
    			startList.add((Unit) list.get(i).clone());
    	}
    	
    	units.insertAfter(startList, start);
    }

    
    private static void addMethodToClass(String className,String fieldName,String methodName)
    {
    	SootClass sc=Scene.v().loadClassAndSupport(className);
    	
    	SootMethod begin = new SootMethod(methodName,new ArrayList<Type>() ,VoidType.v(), Modifier.PUBLIC);
    	sc.addMethod(begin);
    	JimpleBody body = Jimple.v().newBody(begin);
    	begin.setActiveBody(body);
    	
    	
    	 PatchingChain<Unit> units = body.getUnits();
    	 
    	 // add "r0 := @this: className"
         Local r0 = Jimple.v().newLocal("r0", RefType.v(className));
         body.getLocals().add(r0);
         
         units.add(Jimple.v().newIdentityStmt(r0, Jimple.v().newThisRef(RefType.v(className))));
             
             
         // $r1 = r0.<usc.sql.testcase.LoopBranch: android.database.sqlite.SQLiteDatabase db>
         Local $r1 = Jimple.v().newLocal("$r1", RefType.v("android.database.sqlite.SQLiteDatabase"));
         body.getLocals().add($r1);
         units.add(Jimple.v().newAssignStmt($r1, Jimple.v().newInstanceFieldRef(r0, sc.getField(fieldName,RefType.v("android.database.sqlite.SQLiteDatabase")).makeRef())));

         // virtualinvoke $r1.<android.database.sqlite.SQLiteDatabase: void beginTransaction()>()    
         SootMethod toCallBegin = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void "+methodName+"()");
         units.add(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr($r1,toCallBegin.makeRef())));           
         
         // insert "return"
         units.add(Jimple.v().newReturnVoidStmt());
         body.validate();
         
         
/*			CFGInterface cfgNew = new SootCFG(body.getMethod().getSignature(),body.getMethod());
			System.out.println(cfgNew.toDot());
			for(NodeInterface n:cfgNew.getAllNodes())
			{
				
				System.out.println(n.getOffset().toString()+" "+((Node)n).getActualNode());
			}*/
         
    }
}