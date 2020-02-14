package james;

import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class TimeTreeNode {

    private List<TimeTreeNode> children;
    private TimeTreeNode parent = null;
    private int index;

    double age = 0.0;
    String id = null;

    Map<String, Object> metaData = new TreeMap<>();

    TimeTree tree;

    public TimeTreeNode(String id, TimeTree tree) {
        this.id = id;
        this.tree = tree;
        age = 0.0;
    }

    public TimeTreeNode(double age, TimeTreeNode[] children) {
        this.age = age;
        this.children = Arrays.asList(children);
        for (TimeTreeNode child : children) {
            child.parent = this;
        }
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children == null || children.size() == 0;
    }

    public double getAge() {
        return age;
    }

    public List<TimeTreeNode> getChildren() {
        return children;
    }

    public String getId() {
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

    public TimeTreeNode getParent() {
        return parent;
    }

    public String toString() {
        if (isLeaf()) return getId();
        return super.toString();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void addChild(TimeTreeNode child) {
        children.add(child);
        child.parent = this;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getChildCount() {
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

        for (TimeTreeNode child : children)
            child.getAllLeafNodes(leafNodes);
    }

    /**
     * sorts nodes in children according to lowest numbered label in subtree
     *
     * @return
     */
    public void sort() {

        for (TimeTreeNode child : children) {
            child.sort();
        }
        children.sort(Comparator.comparingInt(o -> o.index));
    }
}
