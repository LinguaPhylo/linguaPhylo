package lphy;

import beast.core.BEASTInterface;
import lphy.core.distributions.Utils;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * A Birth-death tree generative distribution
 */
public class RhoSampleTree implements GenerativeDistribution<TimeTree> {

    final String treeParamName;
    final String rhoParamName;
    private Value<TimeTree> tree;
    private Value<Double> rho;

    RandomGenerator random;

    public RhoSampleTree(@ParameterInfo(name = "tree", description = "the full tree to sample") Value<TimeTree> tree,
                         @ParameterInfo(name = "rho", description = "the probability that each tip at time zero is sampled") Value<Double> rho) {

        this.tree = tree;
        this.rho = rho;
        this.random = Utils.getRandom();

        treeParamName = getParamName(0);
        rhoParamName = getParamName(1);
    }


    @GeneratorInfo(name="RhoSampleTree", description="A tree sampled from a larger tree by selecting tips at time zero with probability rho.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        TimeTree sampleTree = new TimeTree(tree.value());
        double p = rho.value();

        List<TimeTreeNode> sampleTips = new ArrayList<>();

        while (sampleTips.size() == 0) {
            for (TimeTreeNode node : sampleTree.getNodes()) {
                if (node.isLeaf() && node.getAge() == 0.0 && random.nextDouble() < p) {
                    sampleTips.add(node);
                }
            }
        }
        System.out.println("Sample tree has " + sampleTips.size() + " tips.");

        for (TimeTreeNode tip : sampleTips ) {
            markNodeAndDirectAncestors(tip);
        }

        removeUnmarkedNodes(sampleTree.getRoot());

        TimeTreeNode newRoot = getFirstNonSingleChildNode(sampleTree.getRoot());
        if (!newRoot.isRoot()) {
            newRoot.getParent().removeChild(newRoot);
        }

        removeSingleChildNodes(newRoot);

        sampleTree.setRoot(newRoot, true);

        return new RandomVariable<>("\u03C8", sampleTree, this);
    }

    private TimeTreeNode getFirstNonSingleChildNode(TimeTreeNode node) {
        if (node.getChildCount() != 1) return node;
        return getFirstNonSingleChildNode(node.getChildren().get(0));
    }

    private void removeSingleChildNodes(TimeTreeNode node) {
        if (node.getChildCount() == 1) {
            TimeTreeNode grandChild = node.getChildren().get(0);
            TimeTreeNode parent = node.getParent();
            parent.removeChild(node);
            node.removeChild(grandChild);
            parent.addChild(grandChild);
            removeSingleChildNodes(grandChild);
        } else {
            List<TimeTreeNode> copy = new ArrayList<>();
            copy.addAll(node.getChildren());
            for (TimeTreeNode child : copy) {
                removeSingleChildNodes(child);
            }
        }
    }

    private void removeUnmarkedNodes(TimeTreeNode node) {
        if (!isMarked(node)) {
            if (node.isRoot()) throw new RuntimeException("Root should always be marked! Something is very wrong!");
            node.getParent().removeChild(node);
        } else if (!node.isLeaf()) {
            List<TimeTreeNode> copy = new ArrayList<>();
            copy.addAll(node.getChildren());
            for (TimeTreeNode child : copy) {
                removeUnmarkedNodes(child);
            }
        }

    }

    private boolean isMarked(TimeTreeNode node) {
        Object mark = node.getMetaData("mark");
        return mark != null;
    }

    private void markNodeAndDirectAncestors(TimeTreeNode node) {
        if (node != null) {
            node.setMetaData("mark", true);
            markNodeAndDirectAncestors(node.getParent());
        }
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        map.put(rhoParamName, rho);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(rhoParamName)) rho = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
    
    public BEASTInterface toBEAST(BEASTInterface value, Map beastObjects) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + ".toBEAST not implemented yet!");
    }
}
