package CallGraph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;

public class NewNode {
    private SootMethod method;
    private Set<NewNode> children;
    private Set<NewNode> parent;
    private int order = -1;
    private int lowLink = -1;
    private boolean onStack = false;
    protected static int ID=0;
    protected String offset;
    public NewNode(SootMethod m) {
        this.method = m;
        this.children = new HashSet<NewNode>();
        this.parent = new HashSet<NewNode>();
        this.offset = ID+"";
        ID++;

    }

    public boolean OrderAssigned() {
        return order != -1;
    }

    public void SetOrder(int o) {
        this.order = o;
    }

    public int GetOrder() {
        return this.order;
    }
    
    public boolean lowLinkAssigned() {
        return lowLink != -1;
    }

    public void SetLowLink(int o) {
        this.lowLink = o;
    }

    public int GetLowLink() {
        return this.lowLink;
    }
    
    public void SetOnStack(boolean b) {
        this.onStack = b;
    }

    public boolean isOnStack() {
        return this.onStack;
    }
    public void setMethod(SootMethod sm)
    {
    	this.method = sm;
    }
    public SootMethod getMethod() {
        return this.method;
    }

    public Set<NewNode> getChildren() {
        return this.children;
    }

    public Set<NewNode> getParent() {
        return this.parent;
    }

    public void addChild(NewNode c) {
        this.children.add(c);
    }

    public void addParent(NewNode p) {
        this.parent.add(p);
    }
    
    public void removeChild(NewNode c){
    	this.children.remove(c);
    }
    
    public void removeParent(NewNode p){
    	this.parent.remove(p);
    }

    public int getIndgree() {
        return this.parent.size();
    }

    public int getOutdgree() {
        return this.children.size();
    }

    public String toString() {
    	if(method!=null)
    		return method.getSignature();
    	else
    		return "dummy";
    }
    
    public String getOffSet()
    {
    	return offset;
    }
    public String toDot(){
    	return offset + " [label=\""+toString()+"\"];";
    }

}
