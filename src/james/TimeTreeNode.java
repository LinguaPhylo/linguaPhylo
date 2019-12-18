package james;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by adru001 on 18/12/19.
 */
public class TimeTreeNode {

    private List<TimeTreeNode> children;
    private TimeTreeNode parent = null;

    double age = 0.0;
    String id = null;

    TimeTree tree;

    public TimeTreeNode(String id, TimeTree tree) {
        this.id = id;
        this.tree = tree;
        age = 0.0;
    }

    public TimeTreeNode(double age, TimeTreeNode[] children) {
        this.age = age;
        this.children = Arrays.asList(children);
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
}
