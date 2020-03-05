package james;

import james.graphicalModel.Value;
import james.app.HasComponentView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adru001 on 17/12/19.
 */
public class TimeTree implements HasComponentView<TimeTree> {

    TimeTreeNode rootNode;

    private List<TimeTreeNode> nodes;

    // number of leaves
    int n = 0;

    public void setRoot(TimeTreeNode timeTreeNode) {
        rootNode = timeTreeNode;
        rootNode.tree = this;
        nodes = new ArrayList<>();

        fillNodeList(rootNode);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public List<TimeTreeNode> getNodes() {
        return nodes;
    }

    private int fillNodeList(TimeTreeNode node) {
        if (node.isRoot()) {
            nodes.clear();
            n = 0;
        }

        node.tree = this;

        if (node.isLeaf()) {
            nodes.add(node);
            n += 1;
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                fillNodeList(child);
            }
            nodes.add(node);
        }

        return nodes.size();
    }

    public int n() {
        return n;
    }

    @Override
    public JComponent getComponent(Value<TimeTree> timeTreeValue) {
        TimeTreeComponent component = new TimeTreeComponent(timeTreeValue.value());
        return component;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        //builder.append("\"");
        toNewick(rootNode, builder);
        //builder.append("\"");
        return builder.toString();
    }

    private void toNewick(TimeTreeNode node, StringBuilder builder) {
        if (node.isLeaf()) {
            builder.append(node.id);
        } else {
            builder.append("(");
            List<TimeTreeNode> children = node.getChildren();
            toNewick(children.get(0), builder);
            for (int i = 1; i < children.size(); i++) {
                builder.append(",");
                toNewick(children.get(i), builder);
            }
            builder.append(")");
        }
        if (node.isRoot()) {
            builder.append(":0.0;");
        } else {
            builder.append(":");
            builder.append(node.getParent().age-node.age);
        }
    }

    public TimeTreeNode getRoot() {
        return rootNode;
    }
}
