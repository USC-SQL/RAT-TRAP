package usc.sql.detection.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.junit.Test;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Trap;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.EqExpr;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.LeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocalBox;
import soot.options.Options;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import sql.global.InterReachingDefinition;
import sql.global.Pair;
import sql.global.ReachingDefinition;
import sql.string.LayerRegion;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.BuildCFGs;
import edu.usc.sql.graphs.cfg.CFGInterface;
import junit.framework.TestCase;

public class ExceptionTest extends TestCase {
	

	private void Checker(String p1,String p2, String p3)
	{


		
        //Options.v().set_src_prec(Options.src_prec_apk);
      	Options.v().set_android_jars("/home/yingjun/Documents/sqlite/sqlAndroid");
      	Options.v().set_whole_program(true);
      	Options.v().set_verbose(false);
      	Options.v().set_keep_line_number(true);
      	Options.v().set_keep_offset(true);
      	Options.v().set_allow_phantom_refs(true);
      	//Scene.v().loadClassAndSupport("android.database.sqlite.SQLiteDatabase");
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						p1,
						p2);
		System.out.println(result);
		CFGInterface cfg = result
				.get(p3);
		
		//System.out.println(cfg.toDot());
		

		
	
		Unit targetU = null;

		System.out.println(cfg.toDot());
		
		for(NodeInterface n:cfg.getAllNodes())
		{
			System.out.println(n.getOffset().toString()+" "+((Node)n).getActualNode());
			Stmt u = (Stmt) ((Node)n).getActualNode();

			
		}
		SootMethod targetMethod = Scene.v().getSootClass("usc.sql.testcase.Exception").getMethod("void test1(android.database.sqlite.SQLiteDatabase,java.lang.String)");
		System.out.println(targetMethod);
		for(Unit u : targetMethod.retrieveActiveBody().getUnits())
		{
			System.out.println("WOW: " + u + "->"+u.getBoxesPointingToThis());
			if(u instanceof IfStmt)
			{
				System.out.println(((IfStmt) u).getCondition());
				Expr ex = (Expr) ((IfStmt) u).getCondition();
				if(ex instanceof EqExpr)
				{
					System.out.println(((EqExpr) ex).getOp2().getType());
				}
			}
		}
		
		for(Trap t:targetMethod.getActiveBody().getTraps())
		{
			System.out.println(t);
			System.out.println(targetMethod.getActiveBody().getUnits().getPredOf(t.getEndUnit()));
			Local exp = Jimple.v().newLocal("$exp", RefType.v("java.lang.Throwable"));
			
			Stmt s = Jimple.v().newIdentityStmt(exp, Jimple.v().newCaughtExceptionRef());
			

			Stmt go = Jimple.v().newGotoStmt(s);
			System.out.println("YOY"+go);
			
			Stmt ts = Jimple.v().newThrowStmt(exp);
			
			System.out.println(t.getHandlerUnit().equals(t.getEndUnit()));
		}
		
		SootMethod test2 = Scene.v().getSootClass("usc.sql.testcase.Exception").getMethod("void test2(android.database.sqlite.SQLiteDatabase,java.lang.String)");

		Body b =test2.retrieveActiveBody();
		
		Stmt target = null;
		Stmt end = null;
		for(Unit u : b.getUnits())
		{
			if(u.toString().contains("isOpen"))
				target = (Stmt) u;
			else if(u.toString().contains("return"))
				end = (Stmt) u;
			System.out.println(u);
		}
		
		
		PatchingChain<Unit> units = b.getUnits();
		Local db = (Local) target.getInvokeExpr().getUseBoxes().get(0).getValue();

		
		//SootMethod toCallBegin = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void beginTransaction()");
		//units.insertBefore(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallBegin.makeRef())), target);
		
		List<Unit> set = new ArrayList<>();
		//SootMethod toCallSuc = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void setTransactionSuccessful()");
		//units.insertBefore(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallSuc.makeRef())), end);
		
		
		//SootMethod toCallEnd = Scene.v().getSootClass("android.database.sqlite.SQLiteDatabase").getMethod("void endTransaction()");
		//units.insertBefore(Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr(db,toCallEnd.makeRef())), end);
		
		//insert after the begin node 
		//1. beginTransaction
		
		//insert before the end node
		//1. setTransactionSuccessful
		//2. goto endTransaction
		//3. endTransaction
		
		//insert a handler
		//1. $exp := @caughtexception
		//2. thr = $exp
		//3. endTransaction
		//4. throw thr
		
		//insert a Trap
		//begin unit: loop header
		//end unit: $exp := @caughtexception
		//handler: $exp:= @caughtexception
		
		/*
		Local exp = Jimple.v().newLocal("$exp", RefType.v("java.lang.Throwable"));		
		Stmt s = Jimple.v().newIdentityStmt(exp, Jimple.v().newCaughtExceptionRef());
		Stmt go = Jimple.v().newGotoStmt(s);		
		Stmt ts = Jimple.v().newThrowStmt(exp);
		*/
	}
	public void Print(String p1,String p2, String p3) {
		
        //Options.v().set_src_prec(Options.src_prec_apk);
      	Options.v().set_android_jars("/home/yingjun/Documents/sqlite/sqlAndroid");
      	Options.v().set_whole_program(true);
      	Options.v().set_verbose(false);
      	Options.v().set_keep_line_number(true);
      	Options.v().set_keep_offset(true);
      	Options.v().set_allow_phantom_refs(true);
      	//Scene.v().loadClassAndSupport("android.database.sqlite.SQLiteDatabase");
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						p1,
						p2);
		System.out.println(result);
		CFGInterface cfg = result
				.get(p3);
		for(NodeInterface n:cfg.getAllNodes())
		{
			System.out.println(n.getOffset().toString()+" "+((Node)n).getActualNode());
			Stmt u = (Stmt) ((Node)n).getActualNode();

			
		}
	}
	@Test
	public void test1() {

		Set<Pair<Integer,Integer>> gt = new HashSet<>();
		gt.add(new Pair<Integer,Integer>(6,1));
		//Checker("/home/yingjun/Documents/eclipse/workspace/SQLiteDetection/bin/","usc/sql/testcase/Exception.class","<usc.sql.testcase.Exception: void test1(android.database.sqlite.SQLiteDatabase,java.lang.String)>" );
		
	    Print("/home/yingjun/Documents/eclipse/workspace/SQLiteDetection/bin/","usc/sql/testcase/Polymorpic.class","<usc.sql.testcase.Polymorpic: void test1()>" );
	}
	

	

}
