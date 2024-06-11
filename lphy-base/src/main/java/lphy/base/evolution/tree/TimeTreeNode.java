package lphy.base.evolution.tree;

import lphy.base.evolution.Taxon;
import lphy.core.model.annotation.MethodInfo;

import java.util.*;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class TimeTreeNode {

    private ArrayList<TimeTreeNode> children = new ArrayList<>();
    private TimeTreeNode parent = null;
    private int index;
    private int leafIndex = -1;

    double age = 0.0;
    String id = null;

    private static final double zeroBranchLengthTolerance = 1e-15;

    SortedMap<String, Object> metaData = new TreeMap<>();

    TimeTree tree;

    public TimeTreeNode(String id, TimeTree tree) {
        this.id = id;
        this.tree = tree;
        age = 0.0;
    }

    public TimeTreeNode(double age) {
        this.age = age;
    }

    public TimeTreeNode(double age, TimeTreeNode[] children) {
        this.age = age;
        this.children = new ArrayList<>();
        Collections.addAll(this.children, children);
        for (TimeTreeNode child : children) {
            child.parent = this;
        }
    }

    public TimeTreeNode(Taxon taxon, TimeTree tree) {
        this.age = taxon.getAge();
        this.id = taxon.getName();
        this.tree = tree;
    }

    TimeTreeNode deepCopy(TimeTree tree) {
        TimeTreeNode copy = new TimeTreeNode(id, tree);
        copy.id = id;
        copy.index = index;
        copy.age = age;
        copy.leafIndex = leafIndex;
        copy.metaData = metaData;
        for (TimeTreeNode child : children) {
            copy.addChild(child.deepCopy(tree));
        }
        return copy;
    }

    public boolean isRoot() {
        return parent == null;
    }

    /**
     * @return true if this node has no parent, and has one child.
     */
    public boolean isOrigin() {
        return parent == null && getChildCount() == 1;
    }

    public boolean isSingleChildNonOrigin() {
        return parent != null && getChildCount() == 1;
    }

    /**
      * @return true if this node is a direct ancestor either because it has a single child (and not the origin) or because it is a leaf child attached to it's parent by a zero branch length
     */
    public boolean isDirectAncestor() {
        return (isSingleChildNonOrigin()) || (isLeaf() && (getParent().age - age) <= zeroBranchLengthTolerance);
    }

    public final boolean isLeaf() {
        return children == null || children.size() == 0;
    }

    @MethodInfo(description="Age of the node.")
    public final double getAge() {
        return age;
    }

    public List<TimeTreeNode> getChildren() {
        return children;
    }

    public final String getId() {
        return id;
    }

    public void setBranchRate(Double rate) {
        setMetaData("rate", rate);
    }

    public Double getBranchRate() {
        return (Double)getMetaData("rate");
    }

    public void setMetaData(String key, Object value) {
        metaData.put(key, value);
    }

    public Object getMetaData(String key) {
        return metaData.get(key);
    }

    public void removeMetaData(String key) {
        metaData.remove(key);
    }

    public SortedMap<String, Object> getMetaData() {
        return metaData;
    }

    public TimeTreeNode getParent() {
        return parent;
    }

    public void setParent(TimeTreeNode newParent) {
        parent = newParent;
    }

    public String toString() {
        if (isLeaf()) return getId();
        return super.toString();
    }

    public final int getIndex() {
        return index;
    }

    public final void setIndex(int index) {
        this.index = index;
    }

    public final int getLeafIndex() {
        return leafIndex;
    }

    public final void setLeafIndex(int index) {
        this.leafIndex = index;
    }


    public void addChild(TimeTreeNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        if (child != null) child.parent = this;
    }

    public void removeChild(TimeTreeNode child) {
        child.parent = null;
        children.remove(child);
    }


    public final void setAge(double age) {
        this.age = age;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final int getChildCount() {
        if (children == null) return 0;
        return children.size();
    }

    /**
     * get all leaf node under this node, if this node is leaf then list.size() = 0.
     *
     * @return
     */
    public List<TimeTreeNode> getAllLeafNodes() {
        final List<TimeTreeNode> leafNodes = new ArrayList<>();
        if (!this.isLeaf()) getAllLeafNodes(leafNodes);
        return leafNodes;
    }

    // recursive
    public void getAllLeafNodes(final List<TimeTreeNode> leafNodes) {
        if (this.isLeaf()) {
            leafNodes.add(this);
        }

        if (!isLeaf()) {
            for (TimeTreeNode child : children) {
                child.getAllLeafNodes(leafNodes);
            }
        }
    }

    /**
     * get all leaf node names under this node, if this node is leaf then list.size() = 0.
     * @return array of all the leaf node names
     */
    @MethodInfo(description = "get all leaf node names under the node.")
    public String[] getAllLeafNodeNames(){
        final List<TimeTreeNode> leafNodes = new ArrayList<>();
        if (!this.isLeaf()) getAllLeafNodes(leafNodes);
        final String[] nodeNames = new String[leafNodes.size()];
        for (int i = 0 ; i<leafNodes.size(); i++){
            nodeNames[i] = leafNodes.get(i).getId();
        }
        return nodeNames;
    }

    /**
     * sorts nodes in children according to lowest numbered label in subtree
     */
    public void sort() {

        if (!isLeaf()) {
            for (TimeTreeNode child : children) {
                child.sort();
            }
            children.sort(Comparator.comparingInt(o -> o.index));
        }
    }

    /**
     * Get total node count including this node and all descendants.
     * @return
     */
    public int getTotalDescendantNodeCount() {
        if (isLeaf()) return 1;
        int count = 1;
        for (TimeTreeNode child : getChildren()) {
            count += child.getTotalDescendantNodeCount();
        }
        return count;
    }

    /**
     * @param i
     * @return the i'th child (zero-based index) or null if the i'th child doesn't exist.
     */
    public TimeTreeNode getChild(int i) {
        if (index >= 0 && i < children.size()) return children.get(i);
        return null;
    }

    public TimeTreeNode getLeft() {
        if (!isLeaf()) return children.get(0);
        return null;
    }

    public TimeTreeNode getRight() {
        if (!isLeaf()) return children.get(1);
        return null;
    }

    public void setLeft(TimeTreeNode left) {
        if (children.size() > 0) {
            children.set(0, left);
            left.setParent(this);
        } else {
            addChild(left);
        }
    }

    public void setRight(TimeTreeNode right) {
        if (children.size() > 1) {
            children.set(1, right);
        } else {
            if (children.size() < 1) {
                addChild(null);
            }
            children.add(right);
        }
        right.setParent(this);
    }

    /**
     * @return the total number of leaves under this node, including this node.
     */
    public int countLeaves() {
        if (isLeaf()) return 1;
        int leafCount = 0;
        for (TimeTreeNode child : getChildren()) {
            leafCount += child.countLeaves();
        }
        return leafCount;
    }

    public double getBranchDuration() {
        if (!isRoot()) {
            return getParent().age - age;
        }
        return 0.0;
    }

    /**
     * @return  if this node is a leave node and age == 0.
     * @see #isExtant(double)
     */
    public boolean isExtant() {
        return isExtant(1E-12);
    }

    /**
     * @param epsilon   to handle precision err. If 0 or greater than 1,
     *                  then the original age will be used.
     * @return    if this node is a leave node and age == 0.
     */
    public boolean isExtant(final double epsilon) {
        double preciseAge = age;
        // handle precision err using epsilon, e.g. 1E-12
        if (epsilon > 0.0 && epsilon < 1.0)
            preciseAge = Math.round(preciseAge / epsilon) * epsilon;
        return isLeaf() && preciseAge == 0.0;
    }
}

