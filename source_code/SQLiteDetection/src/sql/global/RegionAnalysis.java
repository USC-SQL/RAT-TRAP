package sql.global;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import sql.string.LayerRegion;
import sql.string.RegionNode;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import edu.usc.sql.graphs.cfg.SootCFG;

public class RegionAnalysis {
	
	private Map<RegionNode, Boolean> markedforRTree = new HashMap<>();

	private Map<EdgeInterface,RegionNode> backedges = new HashMap<>();
	private CFGInterface cfg;
	private Set<String> targetAPI;
	private Set<String> rSum;
	private String par;
	private RegionNode parNode;
	private Map<NodeInterface,String> modToRegion;
	private Map<NodeInterface, RegionNode> modToRegionNode;
	private Map<String,SootMethod> targetMethod;
	private Map<NodeInterface,CFGInterface> preStoreCFG;
	private Map<NodeInterface,String> modToSig;
	
	public RegionAnalysis(CFGInterface cfg,Set<String> targetAPI,Set<String> rSum,String par,RegionNode parNode,Map<NodeInterface,String> modToRegion,Map<NodeInterface, RegionNode> modToRegionNode, Map<String,SootMethod> targetMethod,Map<NodeInterface,CFGInterface> preStoreCFG,Map<NodeInterface,String> modToSig)
	{
		this.cfg = cfg;
		this.targetAPI = targetAPI;
		this.rSum = rSum;
		this.par = par;
		this.parNode = parNode;
		this.modToRegion = modToRegion;
		this.modToRegionNode = modToRegionNode;
		this.targetMethod =targetMethod;
		this.preStoreCFG = preStoreCFG;
		this.modToSig = modToSig;

		
		LayerRegion lr = new LayerRegion(cfg);
		
		RegionNode root = lr.getRoot();

		//may move this before LayerRegion lr = new LayerRegion(cfg);
		removeExceptionBlock(cfg);
		//System.out.println(cfg.toDot());
		
		Dominator d = lr.getDominator();
		PostDominator pd = new PostDominator(cfg.getAllNodes(),cfg.getAllEdges(),cfg.getExitNode());
		Map<NodeInterface, NodeInterface> imd = d.computeImmediateDominator();
		Map<NodeInterface, NodeInterface> impd = pd.computeImmediatePostDominator();
		
		
		
		for (RegionNode rn : lr.getAllRegionNode()) {
			
			markedforRTree.put(rn, false);
			backedges.put(rn.getBackEdge(),rn);
			
			Set<NodeInterface> endNodes = new HashSet<>();
			
			NodeInterface impdNode = impd.get(rn.getBackEdge().getDestination());

			if(impdNode==null)
				continue;
			
			//if the impdNode is not the exit node
			if(((Node)impdNode).getActualNode()!=null)
			{
				//if the impdNode is not inside the loop
				if(!rn.getNodeList().contains(impdNode))
						endNodes.add(impdNode);
				//if the impdNode is inside the loop, get the first node that is outside the loop and post-dominate the loop entry
				else
				{
					NodeInterface current = impdNode;
					while(rn.getNodeList().contains(current))
					{
						current = impd.get(current);
					}
					
					if(((Node)current).getActualNode()==null)
					{
						Map<NodeInterface, Boolean> marked = new HashMap<>();
						for(NodeInterface n:cfg.getAllNodes())
						{
							marked.put(n, false);
						}
						Set<NodeInterface> dfsNode = new HashSet<>();
						dfs(rn.getBackEdge().getDestination(),marked,dfsNode);
						for(EdgeInterface e:current.getInEdges())
							if(dfsNode.contains(e.getSource()))
							{
								endNodes.add(e.getSource());
							}
					}
					else
					endNodes.add(current);
				}
			}
			//if the impdNode is the exit node
			else
			{
				Map<NodeInterface, Boolean> marked = new HashMap<>();
				for(NodeInterface n:cfg.getAllNodes())
				{
					marked.put(n, false);
				}
				Set<NodeInterface> dfsNode = new HashSet<>();
				dfs(rn.getBackEdge().getDestination(),marked,dfsNode);
				for(EdgeInterface e:impdNode.getInEdges())
					if(dfsNode.contains(e.getSource()))
					{
						endNodes.add(e.getSource());
					}
			}
			

			rn.setEndNode(endNodes);
			rn.setBeginNode(imd.get(rn.getBackEdge().getDestination()));
			
			/*
			NodeInterface beginTry = null;
			NodeInterface current = rn.getBackEdge().getDestination();
			while(!((Stmt)((Node)current).getActualNode() instanceof IfStmt))
			{
				Set<EdgeInterface> outs = current.getOutEdges();
				if(outs.size()>1)
				{
					System.err.println("Begin Try: Branches exit!!");
					break;
				}
				else
				{
					current = outs.iterator().next().getDestination();
				}
			}
			for(EdgeInterface edge:current.getOutEdges())
			{
				if(rn.getNodeList().contains(edge.getDestination()))
				{
					beginTry = edge.getDestination();
					break;
				}
			}
			rn.setBeginTryNode(beginTry);
			*/
	
		}

		
		
		if(rSum.contains(cfg.getSignature()))
		{
			dfsRegionTree(root);
			reconstructCFG(cfg);
		}
		else
		{
			reconstructCFG(cfg);
		}
		
	}
	
	private void dfsRegionTree(RegionNode root)
	{
		markedforRTree.put(root, true);
		
		
		for (RegionNode child : root.getChildren()) {
			if (!markedforRTree.get(child))
			{				
				dfsRegionTree(child);
			}					
	
		}	

		markTargetRegionNode(root);
		

	}
	
	
	private void markTargetRegionNode(RegionNode rn)
	{
		Set<NodeInterface> remainNodes = new HashSet<>();
		remainNodes.addAll(rn.getNodeList());
		for(RegionNode chil:rn.getChildren())
		{
			remainNodes.removeAll(chil.getNodeList());
		}
		
		if(rn.getBackEdge()!=null)
		{
			Unit loopEntry = (Unit) ((Node)rn.getBackEdge().getDestination()).getActualNode();
			int loopEntryOffset = -1;
			for(Tag t : loopEntry.getTags())
			{
				if(t instanceof BytecodeOffsetTag)
				{
					loopEntryOffset = ((BytecodeOffsetTag) t).getBytecodeOffset();
				}
			}
			for(NodeInterface n:remainNodes)
			{
				Unit actualNode = (Unit) ((Node)n).getActualNode();
				if(actualNode!=null)
				{
					
					if(((Stmt)actualNode).containsInvokeExpr())
					{
						
						String sig= ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();
						
						
						modToRegionNode.put(n, rn);
						if(targetAPI.contains(sig))
						{
														
							if(modToRegion.get(n)!=null/*||modToSig.get(n)!=null*/)
								System.err.println("Region Analysis: Node Duplication Appeared");
							
							
							//modToRegion.put(n, cfg.getSignature()+"RegionNum:"+rn.getRegionNumber()+",RegionEntry:"+rn.getBackEdge().getDestination().getOffset());
							modToRegion.put(n,cfg.getSignature()+loopEntry.getJavaSourceStartLineNumber()+","+loopEntryOffset);
							
							
							//modToSig.put(n, cfg.getSignature()+actualNode.getJavaSourceStartLineNumber());
						}
						else if(targetMethod.keySet().contains(sig))
						{
							
							CFGInterface newcfg= new SootCFG(sig,targetMethod.get(sig));
							
/*							if(sig.contains("deleteNote"))
							{
								for(NodeInterface node:newcfg.getAllNodes())
									System.out.println(node.getOffset()+" "+((Node)node).getActualNode());
								System.out.println();
							}*/
							preStoreCFG.put(n, newcfg);
							//new RegionAnalysis(newcfg, targetAPI, rSum, cfg.getSignature()+"RegionNum:"+rn.getRegionNumber()+",RegionEntry:"+rn.getBackEdge().getDestination().getOffset(), modToRegion, targetMethod, preStoreCFG,modToSig);
							new RegionAnalysis(newcfg, targetAPI, rSum, cfg.getSignature()+loopEntry.getJavaSourceStartLineNumber()+","+loopEntryOffset, rn, modToRegion, modToRegionNode, targetMethod, preStoreCFG,modToSig);
						}
	
					}
				}
			}
		}
		else
		{
			for(NodeInterface n:remainNodes)
			{
				Unit actualNode = (Unit) ((Node)n).getActualNode();
				if(actualNode!=null)
				{
					if(((Stmt)actualNode).containsInvokeExpr())
					{
						modToRegionNode.put(n, parNode);
						String sig= ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();

						if(targetAPI.contains(sig))
						{						
							if(par != null)
							{
								modToRegion.put(n, par);
								
								//modToSig.put(n, cfg.getSignature()+actualNode.getJavaSourceStartLineNumber());
							}
						}
						else if(targetMethod.keySet().contains(sig))
						{
							CFGInterface newcfg= new SootCFG(sig,targetMethod.get(sig));
							preStoreCFG.put(n, newcfg);
							new RegionAnalysis(newcfg, targetAPI, rSum, par, parNode, modToRegion, modToRegionNode, targetMethod, preStoreCFG,modToSig);

						}
					}
				}
			}

		}
	}
	private void removeExceptionBlock(CFGInterface cfg)
	{
		//remove redundant catch and finally block
		Map<NodeInterface, Boolean> marked = new HashMap<>();
		NodeInterface entry = cfg.getEntryNode();
		Set<EdgeInterface> realEdge = new HashSet<>();
		for(EdgeInterface e: cfg.getEntryNode().getOutEdges())
		{
			if(e.getLabel().equals("real"))
				realEdge.add(e);
		}
		entry.getOutEdges().retainAll(realEdge);
		for(NodeInterface n:cfg.getAllNodes())
		{
			marked.put(n, false);
		}
		Set<NodeInterface> dfsNode = new HashSet<>();
		dfs(entry,marked,dfsNode);
		cfg.getAllNodes().retainAll(dfsNode);
		
		for(NodeInterface n: dfsNode)
		{
			Set<EdgeInterface> remain = new HashSet<EdgeInterface>();
			for(EdgeInterface e:n.getInEdges())
			{
				if(dfsNode.contains(e.getSource()))
					remain.add(e);
			}
			
			n.getInEdges().retainAll(remain);
		}
		
	}
	private void reconstructCFG(CFGInterface cfg)
	{
		//redirect the back edges 
		for(Entry<EdgeInterface,RegionNode> en: backedges.entrySet())
		{
			NodeInterface loopEntry = en.getKey().getDestination();
			Set<EdgeInterface> edgeInsideLoop = new HashSet<>();
			Set<EdgeInterface> edgeOutsideLoop = new HashSet<>();
			for(EdgeInterface e: loopEntry.getOutEdges())
			{	
				if(en.getValue().getNodeList().contains(e.getDestination()))
					edgeInsideLoop.add(e);
				else
					edgeOutsideLoop.add(e);
			}
			loopEntry.getOutEdges().retainAll(edgeInsideLoop);
			loopEntry.getInEdges().remove(en.getKey());
			
			en.getKey().getSource().getOutEdges().remove(en.getKey());
			en.getKey().getSource().getOutEdges().addAll(edgeOutsideLoop);
			for(EdgeInterface e:edgeOutsideLoop)
			{
				e.setSource(en.getKey().getSource());
			}
			
			
			
				
		}
		
}
	
	
	private void dfs(NodeInterface node,Map<NodeInterface, Boolean> marked, Set<NodeInterface> dfsNode) {
		marked.put(node, true);
		dfsNode.add(node);

		for(EdgeInterface e:node.getOutEdges())
		{

				if(!marked.get(e.getDestination()))
					dfs(e.getDestination(),marked,dfsNode);

		}
	}
	
	
}
