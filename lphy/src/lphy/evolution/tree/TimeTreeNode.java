package lphy.evolution.tree;

import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class TimeTreeNode {

    private List<TimeTreeNode> children = new ArrayList<>();
    private TimeTreeNode parent = null;
    private int index;
    private int leafIndex = -1;

    double age = 0.0;
    String id = null;

    SortedMap<String, Object> metaData = new TreeMap<>();

    TimeTree tree;

    public TimeTreeNode(String id, TimeTree tree) {
        this.id = id;
        this.tree = tree;
        age = 0.0;
    }

    public TimeTreeNode(double age, TimeTreeNode[] children) {
        this.age = age;
        this.children = Arrays.asList(children);
        for (int i = 0; i < children.length; i++) {
            children[i].parent = this;
        }
    }

    TimeTreeNode deepCopy(TimeTree tree) {
        TimeTreeNode copy = new TimeTreeNode(id, tree);
        copy.index = index;
        copy.age = age;
        copy.leafIndex = leafIndex;
        for (TimeTreeNode child : children) {
            copy.addChild(child.deepCopy(tree));
        }
        return copy;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public final boolean isLeaf() {
        return children == null || children.size() == 0;
    }

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
        children.remove(child);
        child.parent = null;
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
     * sorts nodes in children according to lowest numbered label in subtree
     *
     * @return
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
        if (!isLeaf()) return children.get(children.size()-1);
        return null;
    }

    public void setLeft(TimeTreeNode left) {
        if (children.size() > 0) {
            children.set(0, left);
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
    }
}
