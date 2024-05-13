package lphy.base.evolution.tree;

import lphy.core.model.annotation.MethodInfo;

import java.util.SortedMap;
import java.util.TreeMap;

public class TimeTreeBranch {

    private TimeTreeNode parentNode = null;
    private TimeTreeNode childNode = null;
    private TimeTreeBranch branch = null;
    SortedMap<String, Object> metaData = new TreeMap<>();
    private int index;
    double parentAge = 0.0;
    double childAge = 0.0;
    double branchLength = 0.0;
    double branchRate = 0.0;
    String id = null;
    TimeTree tree;

    public TimeTreeBranch(String id, TimeTree tree){
        this.id = id;
        this.tree = tree;
        branchLength = 0.0;
    }

    public TimeTreeBranch(TimeTreeNode parentNode, TimeTreeNode childNode){
        this.parentAge = parentNode.getAge();
        this.childAge = childNode.getAge();
        this.parentNode = parentNode;
        this.childNode = childNode;
        branchLength = 0.0;
    }

    public TimeTreeBranch(double branchLength){
        this.branchLength = branchLength;
    }

    TimeTreeBranch deepCopy(TimeTree tree){
        TimeTreeBranch copy = new TimeTreeBranch(id,tree);
        copy.id = id;
        copy.index = index;
        copy.branchLength = branchLength;
        copy.parentNode = parentNode;
        copy.childNode = childNode;
        copy.parentAge = parentAge;
        copy.childAge = childAge;
        copy.branchRate = branchRate;
        copy.addChildNode(childNode.deepCopy(tree));
        return copy;
    }

    public void setBranchLength(Double branchLength){
        setMetaData("branchLength", branchLength);
    }
    public final double getBranchLength(){
        Object length = getMetaData("branchLength");
        if (length instanceof Double) {
            return (Double) length;
        } else {
            // return to default length
            return 0.0;
        }
    }

    // MetaData
    public void setMetaData(String key, Object value) {
        metaData.put(key, value);
    }

    public Object getMetaData(String key) {
        return metaData.get(key);
    }
    public SortedMap<String, Object> getMetaData() {
        return metaData;
    }
    public void removeMetaData(String key) {
        metaData.remove(key);
    }

    public final void setId(String id) {
        this.id = id;
    }
    public final String getId(){
        return id;
    }

    public void setBranchRate(Double branchRate) {
        setMetaData("branchRate", branchRate);
    }

    public Double getBranchRate() {
        return (Double)getMetaData("branchRate");
    }

    @MethodInfo(description = "set the parent of the branch with given node")
    public void setParentNode(TimeTreeNode newParent){
        if (newParent == null) {
            throw new IllegalArgumentException("Parent node cannot be null.");
        }
        parentNode = newParent;
    }

    public final TimeTreeNode getParentNode(){
        if (parentNode == null) {
            throw new IllegalStateException("Parent node is not set.");
        }
        return parentNode;
    }

    public final int getIndex() {
        return index;
    }
    public final void setIndex(int index) {
        this.index = index;
    }

    public void addChildNode(TimeTreeNode childNode){
        branch.setChildNode(childNode);
    }

    public void setChildNode(TimeTreeNode newChild){
        if (newChild == null) {
            throw new IllegalArgumentException("Child node cannot be null.");
        }
        childNode = newChild;
    }
    public final TimeTreeNode getChildNode(){
        if (childNode == null) {
            throw new IllegalStateException("Child node is not set.");
        }
        return childNode;
    }
}
