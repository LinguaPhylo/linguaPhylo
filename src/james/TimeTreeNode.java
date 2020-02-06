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
}
