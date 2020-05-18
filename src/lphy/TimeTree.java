package lphy;

import lphy.app.treecomponent.TimeTreeComponent;
import lphy.graphicalModel.Value;
import lphy.app.HasComponentView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by adru001 on 17/12/19.
 */
public class TimeTree implements HasComponentView<TimeTree> {

    TimeTreeNode rootNode;

    private List<TimeTreeNode> nodes;

    String[] taxaNames = null;

    // number of leaves
    int n = 0;

    public void setRoot(TimeTreeNode timeTreeNode) {
        rootNode = timeTreeNode;
        rootNode.tree = this;
        nodes = new ArrayList<>();

        fillNodeList(rootNode);
        indexNodes(rootNode, new int[]{n});
        // root node now last in list, first n nodes are leaves
        nodes.sort(Comparator.comparingInt(TimeTreeNode::getIndex));
    }

    private void indexNodes(TimeTreeNode node, int[] nextInternalIndex) {
        if (node.isLeaf()) {
            node.setIndex(node.getLeafIndex());
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                indexNodes(child, nextInternalIndex);
            }
            node.setIndex(nextInternalIndex[0]);
            nextInternalIndex[0] += 1;
        }
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getSingleChildNodeCount() {
        int count = 0;
        for (TimeTreeNode node : nodes) {
            if (node.getChildCount() == 1) count += 1;
        }
        return count;
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

        if (node.getMetaData("remove") != null) {
            throw new RuntimeException("A node that should be removed has not been!" + node.id);
        }

        if (node.isLeaf()) {
            nodes.add(node);
            if (node.getLeafIndex() == -1) node.setLeafIndex(n);
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

    public String[] getTaxaNames() {
        if (taxaNames == null) {
            taxaNames = new String[n()];
            for (TimeTreeNode node : getNodes()) {
                if (node.isLeaf()) {
                    taxaNames[node.getLeafIndex()] = node.getId();
                }
            }
        }
        return taxaNames;
    }

    public TimeTreeNode getNodeByIndex(int index) {
        TimeTreeNode node = getNodes().get(index);
        if (node.getIndex() != index) throw new RuntimeException();
        return node;
    }
}
