package CallGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Sources;
import soot.jimple.toolkits.callgraph.Targets;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import edu.usc.sql.graphs.cfg.SootCFG;

/*class Counter {
    private int cnt = 0;

    public void inCreaseCounter() {
        this.cnt++;
    }

    public int getCnt() {
        return cnt;
    }
}
*/
public class StringCallGraph {
    private Set<NewNode> nodes;
    private Set<NewNode> heads;
    private Set<NewNode> nodesinscc;
    private LinkedList<NewNode> RTOlist = new LinkedList<NewNode>();
    private Map<String,NewNode> RTOmap = new HashMap<String,NewNode>();
    private HashMap<NewNode, Integer> indegredmap=new HashMap<NewNode, Integer>();
    private String entry = "entry";
    private Set<NewNode> selfSCC = new HashSet<>();
    private void Visit(NewNode n, List<NewNode> list, Set<NewNode> visited) {
        if (visited.contains(n))
            return;
        visited.add(n);

        for (NewNode w : n.getChildren()) {
            Visit(w, list, visited);
        }
        list.add(n);
    }
    //reverse topology ordering
    //refer to the first algorithm of https://en.wikipedia.org/wiki/Topological_sorting
    private static Set<String> sigSet = new HashSet<String>();
    private static int count = 0;
    //strongly connected component detection
    private void isLoopNode(Iterator<NewNode> iter, NewNode m){
    	//System.out.print(++count);
    	sigSet.add(m.getMethod().getSignature());
    	if(!iter.hasNext()) {
    		System.out.println("+++ entry parent: " + m.getMethod().getSignature());
    		return;
    	}
    	while(iter.hasNext()){
    		NewNode n = iter.next();
    		System.out.println("\t" + getNodeString(n.getMethod().getSignature()) + " -> " + getNodeString(m.getMethod().getSignature()) + ";");
    		if(sigSet.contains(n.getMethod().getSignature())) {
    			System.out.println("*** 2nd parent: " + n.getMethod().getSignature());
    			return;
    		}
    		//System.out.println("parent: " + n.getMethod().getSignature());
    		sigSet.add(n.getMethod().getSignature());
    		isLoopNode(n.getParent().iterator(), n);
    	}
    }
    public static String getNodeString(String str){
		String[] arr = str.split(":"), arr1 = arr[1].split(" ");
		String c = arr[0].substring(arr[0].lastIndexOf('.') + 1), m = arr[1].substring(0, arr[1].lastIndexOf('('));
		String result = "<" + c + ": " + m + ">";
		return result;
	}
    private void RTOsorting(){

    	
		for(NewNode n:nodes)
		{
			indegredmap.put(n,n.getIndgree());
			/*if(n.getMethod().getSignature().contains("com.wisesharksoftware.core.DiskLruCache: boolean remove(")){
				isLoopNode(n.getParent().iterator(), n);
            }*/
		}
		Set<NewNode> visited=new HashSet<NewNode> ();
		Queue<NewNode> S=new LinkedList<NewNode>();
		S.addAll(heads);
		while(!S.isEmpty())
		{
			NewNode n=S.poll();
			visited.add(n);
			if(n.getMethod()!=null)
			{
				RTOlist.addFirst(n);
				RTOmap.put(n.getMethod().getSignature(), n);
			}
			for(NewNode m:n.getChildren())
			{
				/*System.out.println("p: " + n.getMethod().getSignature() + "\tc: " + m.getMethod().getSignature());
				if(!nodes.contains(m)){
					System.out.println("this node is not included in nodes, with indegree: " + m.getIndgree());
				}*/
				int deg=indegredmap.get(m);
				deg--;
				indegredmap.put(m, deg);
				if(deg<=0&& !visited.contains(m))
				{
					S.add(m);
					visited.add(m);
				}
			}
		}
		
	}
    
    public List<CFGInterface> getRTOInterface(){
        List<CFGInterface> r=new LinkedList<CFGInterface>();
        System.out.println("Nodes size: " + nodes.size() + "\tRTOlist size: " + RTOlist.size());
        /*for(NewNode n : nodes){
        	if(!RTOlist.contains(n)){
        		System.out.println("missing node after RTO: " + n.getMethod().getSignature() + "\tIndegree: " + n.getIndgree() + "\tOutdegree: " + n.getOutdgree());
        	}
        }*/
        for(NewNode n:RTOlist)
        {
            if(n.getMethod().isConcrete()){
            	SootCFG cfg = new SootCFG(n.getMethod().getSignature(),n.getMethod());
                r.add(cfg);
                if(n.getMethod().getSignature().contains("com.wisesharksoftware.ad.Banner: void <init>(")){
                	System.out.println("All related loadAd: " + n.getMethod().getSignature());
                }/*else if(cfg.getSignature().contains("onCreate(") && !cfg.getSignature().contains("android.support") && !cfg.getSignature().contains("com.google")){
                	System.out.println("All related loadAd: " + n.getMethod().getSignature());
                }*/
            }
        }
        return r;
    }

    public LinkedList<NewNode> getRTOdering() {

        return RTOlist;
    }
    

    
    public String getAdUnitSignature(Unit u){
    	return u.toString().substring(u.toString().indexOf('<'), u.toString().lastIndexOf('>') + 1);
    }

    public StringCallGraph(){
    	
    }

    public StringCallGraph(CallGraph cg, Set<SootMethod> allmethods) {
        nodes = new HashSet<NewNode>();
        heads = new HashSet<NewNode>();
        nodesinscc = new HashSet<NewNode>();
        HashMap<SootMethod, NewNode> methodToNodeMap = new HashMap<SootMethod, NewNode>();
        for (SootMethod sm : allmethods) {
            NewNode n = new NewNode(sm);
            nodes.add(n);
            methodToNodeMap.put(sm, n);
/*            if(sm.getSignature().contains("insertOrUpdate")){
            	System.out.println("Double check: " + sm.getSignature());
            }*/
        } 
 
       
        for (NewNode n : nodes) {
            SootMethod sm = n.getMethod();
            
            
            Iterator sources = new Sources(cg.edgesInto(sm));
            
                 	
           //System.out.println(sources.hasNext());
           
            while (sources.hasNext()) {
                SootMethod src = (SootMethod) sources.next();

                if (allmethods.contains(src)) {
                    NewNode p = methodToNodeMap.get(src);

                 
                    if(!p.getMethod().getSignature().equals(n.getMethod().getSignature()))
                    {
                        p.addChild(n);
                    	n.addParent(p);
                    }
                    else
                    {
                    	if(!n.getMethod().getSignature().contains("void <clinit>()"))
                    		selfSCC.add(n);
                    }
                       
                    
                }
                
            }
            
           
        }

        
        for (NewNode n : nodes) {
            if (n.getIndgree() == 0)
            {
            	heads.add(n);
            }
       }
        System.out.println("Before SCC Nodes size: " + nodes.size() + "\theads size: " + heads.size());
        SCCTarjanDetection();
        
        
        processSCC(cg, allmethods);
        
        System.out.println("After SCC Nodes size: " + nodes.size() + "\theads size: " + heads.size());
        System.out.println("NodesInSCC size: " + nodesinscc.size());

        
        System.out.println("Nodes size: " + nodes.size() + "\tRTOlist size: " + RTOlist.size());
        RTOsorting();
        System.out.println("Nodes size: " + nodes.size() + "\tRTOlist size: " + RTOlist.size());
        //System.out.println(RTOlist);
    }   
    
    //Tarjan's strongly connected components algorithm
    public static int index = 0;
    public static Stack<NewNode> stack = new Stack<NewNode>(); 
    public static List<List<NewNode>> allSCC = new ArrayList<List<NewNode>>();
    public void SCCTarjanDetection(){
    	for(NewNode n : nodes){
    		if(!n.OrderAssigned())
    		{
    			strongConnect(n);
    		}
    	}
    }    
    public void strongConnect(NewNode v){
    	v.SetLowLink(index);
    	v.SetOrder(index);
    	index++;
    	stack.push(v);
    	v.SetOnStack(true);
    	int min = v.GetLowLink();
    	for(NewNode w : v.getChildren()){
    		if(!w.OrderAssigned()){
    			strongConnect(w);
    		}
    		if(w.GetLowLink() < min){
    			min = w.GetLowLink();
    		}
    	}
    	if(min < v.GetLowLink()){
    		v.SetLowLink(min);
    		return;
    	}
    	List<NewNode> component = new ArrayList<NewNode>();
    	NewNode w;
    	do{
    		w = stack.pop();
    		component.add(w);
    		w.SetLowLink(nodes.size());
    	}while(w != v);

    	if(component.size() >1){
    		for(NewNode n : component){
    			nodesinscc.add(n);
    		}
    		allSCC.add(component);
    	}
    	
    }
    
    public void processSCC(CallGraph cg, Set<SootMethod> allmethods){
    	for(List<NewNode> scc : allSCC){
	    	Set<NewNode> parents = new HashSet<NewNode>();
	    	Set<NewNode> children = new HashSet<NewNode>();
	    	for(NewNode n : scc){
	    		
	    		for(NewNode p : n.getParent()){
	    			if(!scc.contains(p)){
		    			parents.add(p);
		    			p.removeChild(n);
	    			}
	    		}
	    		for(NewNode c : n.getChildren()){
	    			if(!scc.contains(c)){
		    			children.add(c);
		    			c.removeParent(n);
	    			}
	    		}
	    		nodes.remove(n);
	    	}
	    	
	    	
/*	    		GC limit overhead
 * 
 * 				for(NewNode p : parents){
	    		for(NewNode c : children){
	    			p.addChild(c);
	    			c.addParent(p);
	    		}
	    	}*/
	    	
	    	NewNode dummy = new NewNode(null);
	    	for(NewNode p:parents)
	    	{
	    		p.addChild(dummy);
	    		dummy.addParent(p);
	    	}
	    	for(NewNode c:children)
	    	{
	    		dummy.addChild(c);
	    		c.addParent(dummy);
	    	}
	    	nodes.add(dummy);
    	}
    	

    	//set selfSCC as dummy node and set heads
    	
	    	heads = new HashSet<NewNode>();
	    	for (NewNode n : nodes) {
	    		
	    		if(selfSCC.contains(n))
	    		{
	    			
	    			nodesinscc.add(n);
	    			n.setMethod(null);
	    		}
	    		
	            if (n.getIndgree() == 0/*&&n.getMethod()!=null*/)
	            {
	            	heads.add(n);
/*	            	if(n.getMethod().getSignature().contains("onClick"))
	            		System.out.println("head:" + n.getMethod().getSignature());*/
	            }
	        }
    	

    }
    
    public void display() {
        //System.out.println(heads);
        System.out.println("=================================");
        for (NewNode n : nodes) {
            String sig = n.getMethod().getSignature();
            for (NewNode c : n.getChildren()) {
                String log = sig + "->" + c.getMethod().getSignature();
                System.out.println(log);
            }
        }
        System.out.println("=================================");
        System.out.println(nodesinscc);


    }
    
	public Set<String> getParents(String s)
	{
		Set<String> r=new HashSet<String>();
		for(NewNode n:nodes)
		{
			SootMethod sm=n.getMethod();
			if(s.equals(sm.getSignature())){
				Set<NewNode> pset=n.getParent();
				for(NewNode p:pset){
					r.add(p.getMethod().getSignature());
				}
			}
		}
		return r;
	}
	
	public Set<NewNode> getHeads()
	{
		Set<NewNode> nonNullHeads = new HashSet<>();
		for(NewNode n: heads)
			if(n.getMethod()!=null)
				nonNullHeads.add(n);
		return nonNullHeads;
	}
	public String toDot()
	{
		StringBuilder dotGraph = new StringBuilder();
		dotGraph.append("digraph directed_graph {\n\tlabel=\"" + "call graph" + "\";\n");
		dotGraph.append("\tlabelloc=t;\n");
		for (NewNode node : nodes) {
			dotGraph.append("\t"+node.toDot() + "\n");
		}
		for (NewNode node : nodes) {
			if(!node.getChildren().isEmpty())
			{
				for(NewNode c:node.getChildren())
					dotGraph.append("\t"+node.getOffSet()+" -> "+c.getOffSet()+"\n");
			}
		}
		dotGraph.append("}\n");
		return dotGraph.toString();
	}
	public String toDotRel(Set<String> rel)
	{
		StringBuilder dotGraph = new StringBuilder();
		dotGraph.append("digraph directed_graph {\n\tlabel=\"" + "relevant call graph" + "\";\n");
		dotGraph.append("\tlabelloc=t;\n");
		for (NewNode node : nodes) {
			if(rel.contains(node.toString()))
			dotGraph.append("\t"+node.toDot() + "\n");
		}
		for (NewNode node : nodes) {
			if(rel.contains(node.toString())&&!node.getChildren().isEmpty())
			{
				for(NewNode c:node.getChildren())
					if(rel.contains(c.toString()))
					dotGraph.append("\t"+node.getOffSet()+" -> "+c.getOffSet()+"\n");
			}
		}
		dotGraph.append("}\n");
		return dotGraph.toString();
	}
	public Map<String, NewNode> getRTOMap()
	{
		return RTOmap;
	}
	public Set<String> getEntryMethod(String sig)
	{
		Set<String> entries = new HashSet<>();
		NewNode target = RTOmap.get(sig);
		
		Map<NewNode,Boolean> marked = new HashMap<>();;
		for(NewNode n:nodes)
		{
			marked.put(n, false);
		}
		
		//reverse dfs of target node
       Stack<NewNode> st = new Stack<NewNode>();
       st.push(target);
       while(!st.isEmpty()){
    	   NewNode v = st.pop();
           if(!marked.get(v)){
               marked.put(v, true);
               if(v.getIndgree()==0)
               {
            	   if(v.getMethod()!=null)
            		   entries.add(v.getMethod().getSignature());
               }
               for(NewNode parent: v.getParent()){
                   if(!marked.get(parent)){
                       st.push(parent);
                   }
               }
 
           }
       }
       return entries;
		
	}
}
