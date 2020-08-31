package lphy.evolution.tree;

import lphy.app.treecomponent.TimeTreeComponent;
import lphy.evolution.Taxa;
import lphy.evolution.TaxaAges;
import lphy.graphicalModel.Value;
import lphy.app.HasComponentView;

import javax.swing.*;
import java.util.*;

/**
 * Created by adru001 on 17/12/19.
 */
public class TimeTree implements TaxaAges, HasComponentView<TimeTree> {

    TimeTreeNode rootNode;

    private List<TimeTreeNode> nodes;

    String[] taxaNames = null;

    // number of leaves
    int n = 0;

    public TimeTree() {
    }

    public TimeTree(TimeTree treeToCopy) {
        setRoot(treeToCopy.getRoot().deepCopy(this));
    }

    public void setRoot(TimeTreeNode timeTreeNode, boolean reindexLeaves) {
        rootNode = timeTreeNode;
        rootNode.tree = this;
        nodes = new ArrayList<>();

        fillNodeList(rootNode, reindexLeaves);
        indexNodes(rootNode, new int[]{n});
        // root node now last in list, first n nodes are leaves
        nodes.sort(Comparator.comparingInt(TimeTreeNode::getIndex));
    }

    public void setRoot(TimeTreeNode timeTreeNode) {
        setRoot(timeTreeNode, false);
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

    private int fillNodeList(TimeTreeNode node, boolean reindexLeaves) {
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
            if (node.getLeafIndex() == -1 || reindexLeaves) node.setLeafIndex(n);
            n += 1;
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                fillNodeList(child, reindexLeaves);
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
        toNewick(rootNode, builder, true);
        //builder.append("\"");
        return builder.toString();
    }

    private void toNewick(TimeTreeNode node, StringBuilder builder, boolean includeSingleChildNodes) {
        if (!includeSingleChildNodes && node.getChildCount() == 1) {
            System.out.println("Skip single child node " + node.getId());
            toNewick(node.getChildren().get(0), builder, includeSingleChildNodes);
        } else {
            if (node.isLeaf()) {
                builder.append(node.id);
                SortedMap<String, Object> metaData = node.metaData;
                if (metaData.size() > 0) {
                    builder.append("[&");
                    for (Map.Entry<String, Object> entry : metaData.entrySet()) {
                        builder.append(entry.getKey());
                        builder.append("=");
                        builder.append(entry.getValue());
                    }
                    builder.append("]");
                }
            } else {

                builder.append("(");
                List<TimeTreeNode> children = node.getChildren();
                toNewick(children.get(0), builder, includeSingleChildNodes);
                for (int i = 1; i < children.size(); i++) {
                    builder.append(",");
                    toNewick(children.get(i), builder, includeSingleChildNodes);
                }
                builder.append(")");
            }
            if (node.isRoot()) {
                builder.append(":0.0;");
            } else {
                builder.append(":");
                double branchLength = getBranchLength(node, includeSingleChildNodes);
                builder.append(branchLength);
            }
        }
    }

    public Double treeLength() {

        double TL = 0;
        for (TimeTreeNode node : getNodes()) {
            if (!node.isRoot()) {
                TL += node.getParent().age - node.age;
            }
        }
        return TL;
    }

    private double getBranchLength(TimeTreeNode node, boolean includeSingleChildNodes) {
        TimeTreeNode parent = node.getParent();
        if (!includeSingleChildNodes) {
            if (parent.getChildCount() == 1) {
                parent = parent.getParent();
            }
        }
        if (parent != null) return parent.age - node.age;
        return 0.0;
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

    public boolean isUlrametric() {
        for (TimeTreeNode node : getNodes()) {
            if (node.isLeaf() && node.getAge() != 0.0) {
                return false;
            }
        }
        return true;
    }

    public String toNewick(boolean includeSingleChildNodes) {
        StringBuilder builder = new StringBuilder();
        //builder.append("\"");
        toNewick(rootNode, builder, includeSingleChildNodes);
        //builder.append("\"");
        return builder.toString();
    }

    @Override
    public int ntaxa() {
        return n();
    }

    public String[] getTaxa() {
        // defensive copy
        return Arrays.copyOf(taxaNames, taxaNames.length);
    }

    @Override
    public Double[] getAges() {
        Double[] ages = new Double[taxaNames.length];
        for (TimeTreeNode node : getNodes()) {

            if (node.isLeaf()) {
                if (node.getId().equals(taxaNames[node.getIndex()])) {
                    throw new RuntimeException("Assertion failed!");
                }
                ages[node.getIndex()] = node.getAge();
            }
        }
        return ages;
    }
}
