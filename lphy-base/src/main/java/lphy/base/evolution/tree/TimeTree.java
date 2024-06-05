package lphy.base.evolution.tree;

import lphy.base.evolution.HasTaxa;
import lphy.base.evolution.Taxa;
import lphy.core.model.MultiDimensional;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.MethodInfo;
import lphy.core.model.annotation.TypeInfo;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Alexei Drummond on 17/12/19.
 */
@TypeInfo(description = "A phylogenetic tree scaled by time, including the contemporary taxa as a special case.",
        examples = {"jcCoalescent.lphy","simpleSerialCoalescentNex.lphy"})
public class TimeTree implements HasTaxa, MultiDimensional {

    TimeTreeNode rootNode;

    private List<TimeTreeNode> nodes;
    Taxa taxa = null;
    boolean constructedWithTaxa = false;

    // number of leaves
    int n = 0;

    public TimeTree(Taxa taxa) {
        this.taxa = taxa;
        constructedWithTaxa = true;
    }

    public TimeTree() {
    }

    public TimeTree(TimeTree treeToCopy) {
        taxa = treeToCopy.taxa;
        setRoot(treeToCopy.getRoot().deepCopy(this));
    }

    public void setRoot(TimeTreeNode root, boolean reindexLeaves) {

        rootNode = root;
        rootNode.setParent(null);
        rootNode.tree = this;
        nodes = new ArrayList<>();

        fillNodeList(rootNode, reindexLeaves);
        indexNodes(rootNode, new int[]{n});
        // root node now last in list, first n nodes are leaves
        nodes.sort(Comparator.comparingInt(TimeTreeNode::getIndex));

        if (!constructedWithTaxa) taxa = Taxa.createTaxa(root);
    }

    public void setRoot(TimeTreeNode root) {
        setRoot(root, false);
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

    public String toString() {
        StringBuilder builder = new StringBuilder();
        //builder.append("\"");
        toNewick(rootNode, builder, true);
        //builder.append("\"");
        return builder.toString();
    }

    private void toNewick(TimeTreeNode node, StringBuilder builder, boolean includeSingleChildNodes) {
        if (!includeSingleChildNodes && node.getChildCount() == 1) {
            //System.out.println("Skip single child node " + node.getId());
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

    @MethodInfo(description = "get all taxa names")
    public String[] getTaxaNames() {
        return taxa.getTaxaNames();
    }

    public String[] getSpecies() {
        return taxa.getSpecies();
    }

    public TimeTreeNode getNodeByIndex(int index) {
        TimeTreeNode node = getNodes().get(index);
        if (node.getIndex() != index) throw new RuntimeException();
        return node;
    }

    public boolean isUltrametric() {
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
    public int getDimension() {
        return n();
    }

    @Override
    public Taxa getTaxa() {
        return taxa;
    }

    public List<TimeTreeNode> getExtantNodes() {
        return getNodes().stream().filter(TimeTreeNode::isExtant).collect(Collectors.toList());
    }

    public List<TimeTreeNode> getInternalNodes() {
        return getNodes().stream().filter(Predicate.not(TimeTreeNode::isLeaf)).collect(Collectors.toList());
    }

    public List<TimeTreeNode> getLeafNodes() {
        return getNodes().stream().filter(TimeTreeNode::isLeaf).collect(Collectors.toList());
    }

    // methods permitted pass-through to LPhy

    @MethodInfo(description = "the total length of the tree")
    public Double treeLength() {

        double TL = 0.0;
        for (TimeTreeNode node : getNodes()) {
            if (!node.isRoot()) {
                TL += node.getParent().age - node.age;
            }
        }
        return TL;
    }

    @MethodInfo(description = "the age of the root of the tree.",
            category = GeneratorCategory.TREE,
            examples = {"simFossilsCompact.lphy", "simpleBirthDeathSerial.lphy", "simpleCalibratedYule.lphy"})
    public Double rootAge() {

        return getRoot().age;
    }

    @MethodInfo(description = "the total number of nodes in the tree (both leaf nodes and internal nodes).",
            examples = {"yuleRelaxed.lphy"})
    public Integer nodeCount() {
        return getNodeCount();
    }

    @MethodInfo(description = "the total number of branches in the tree (returns nodeCount() - 1)")
    public Integer branchCount() {
        return getNodeCount() - 1;
    }

    @MethodInfo(description = "the total number of extant leaves in the tree (leaf nodes with age 0.0).")
    public Integer extantCount() {
        int count = 0;
        for (TimeTreeNode node : getNodes()) {
            if (node.age == 0.0 && node.isLeaf()) count += 1;
        }
        return count;
    }

    @MethodInfo(description = "the total number of leaf nodes in the tree (leaf nodes with any age, but excluding zero-branch-length leaf nodes, which are logically direct ancestors).")
    public Integer leafCount() {
        int count = 0;
        for (TimeTreeNode node : getNodes()) {
            if (node.isLeaf() && !node.isDirectAncestor()) count += 1;
        }
        return count;
    }

    @MethodInfo(description = "the total number of nodes in the tree that are direct ancestors (i.e. have a single parent and a single child, or have one child that is a zero-branch-length leaf).")
    public Integer directAncestorCount() {
        int count = 0;
        for (TimeTreeNode node : getNodes()) {
            if (node.isDirectAncestor()) count += 1;
        }
        return count;
    }

    @MethodInfo(description = "the taxa of the tree.")
    public Taxa taxa() {
        return getTaxa();
    }

    @MethodInfo(description = "returns true if this tree has an origin node (defined as a root node with a single child.")
    public boolean hasOrigin() {
        return getRoot().isOrigin();
    }

    @MethodInfo(description = "get the oldest internal node under the given maximum age.")
    public TimeTreeNode getOldestInternalNode(Double maxAge) {
        double temp = 0;
        TimeTreeNode tempNode = null;
        for (TimeTreeNode node : getInternalNodes()) {
            double nodeAge = node.getAge();
            if (nodeAge <= maxAge) {
                if (nodeAge > temp) { // if every nodeAge after the 1st "if" are 0s, then this will be always false
                    temp = nodeAge;
                    tempNode = node;
                }
            }
        }
        if (tempNode == null) {
            throw new IllegalArgumentException("The input age should be older than the lowest ancestor, maxAge = " + maxAge);
        } else {
            return tempNode;
        }
    }

    @MethodInfo(narrativeName = "node with given label",description = "get the node by given label.")
    public TimeTreeNode getLabeledNode(String label) {
        List<TimeTreeNode> nodes = getNodes();
        for (TimeTreeNode node : nodes) {
            if (node.getMetaData("label") != null && node.getMetaData("label").equals(label)) {
                return node;
            }
        }
        return null;
    }

    @MethodInfo(description = "get the array of all branch rates in the tree.")
    public Double[] getBranchRates(){
        List<Double> branchRates = new ArrayList<>();
        for (TimeTreeNode node: nodes){
            if (node.getBranchRate() != null) {
                branchRates.add(node.getBranchRate());
            }
        }

        return branchRates.toArray(Double[]::new);
    }
}
