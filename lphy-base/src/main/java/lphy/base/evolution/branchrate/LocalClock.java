package lphy.base.evolution.branchrate;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.Arrays;

public class LocalClock extends DeterministicFunction<TimeTree> {
    public static final String treeName = "tree";
    public static final String cladeArrayName = "clades";
    public static final String cladeRateArrayName = "cladeRates";
    public static final String rootRateName = "rootRate";

    public static final String includeStemName = "includeStem";

    public static final boolean DEFAULT_INCLUDE_STEM = true;

    public LocalClock(
            @ParameterInfo(name = treeName, description = "the tree used to calculate branch rates" ) Value<TimeTree> tree,
            @ParameterInfo(name = cladeArrayName, narrativeName = "given clades", description = "the array of the node names") Value<Object[]> clades,
            @ParameterInfo(name = cladeRateArrayName, narrativeName = "array of rates corresponding with clade", description = "the array of clade rates") Value<Double[]> cladeRates,
            @ParameterInfo(name = rootRateName, description = "the root rate of the tree") Value<Double> rootRate,
            @ParameterInfo(name = includeStemName, narrativeName = "criterion of including stem", description = "whether to include stem of clades, defaults to true", optional = true) Value<Boolean> includeStem){
        if (tree == null) throw new IllegalArgumentException("The tree can't be null!");
        if (clades == null) throw new IllegalArgumentException("The clades can't be null!");
        if (cladeRates == null) throw new IllegalArgumentException("The clade rates can't be null!");
        if (rootRate == null) throw new IllegalArgumentException("The root rate can't be null!");
        if (cladeRates.value().length != clades.value().length) throw new IllegalArgumentException("The clade rates should match the given clades!");
        setParam(treeName, tree);
        setParam(cladeArrayName, clades);
        setParam(cladeRateArrayName, cladeRates);
        setParam(rootRateName, rootRate);
        if (clades.value() != null) {
            for (Object clade : clades.value()) {
                if (!(clade instanceof TimeTreeNode)) {
                    throw new IllegalArgumentException("The clades array should be nodes!");
                }
            }
        }
        if (includeStem == null) {
            setParam(includeStemName, new Value<Boolean>("", DEFAULT_INCLUDE_STEM));
        } else {
            setParam(includeStemName, includeStem);
        }
    }

    @GeneratorInfo(name = "localClock", examples = {"substituteClade.lphy"}, description = "Apply local clock in a phylogenetic tree to generate a tree with " +
            "branch rates. The order of elements in clades and cladeRates array should match. The clades" +
            " should not be overlapped with each other.")
    @Override
    public Value<TimeTree> apply() {
        // get parameters
        TimeTree originalTree = getTree().value();
        Object[] clades = getClades().value();
        Double[] cladeRates = getCladeRates().value();
        Double rootRate = getRootRate().value();
        Boolean includeStem = getIncludeStem().value();

        // make a deep copy of the original tree
        TimeTree tree = new TimeTree(originalTree);

        // set the rates within specified clades
        for (int i = 0; i < clades.length; i++){
            TimeTreeNode oldClade = (TimeTreeNode) clades[i];
            // get the clade in the deep copy tree
            TimeTreeNode clade = tree.getNodeByIndex(oldClade.getIndex());

            double rate = cladeRates[i];
            if (includeStem == null || includeStem) {
                setRate(clade, rate, true);
            } else {
                setRate(clade, rate, false);
            }
        }

        for (TimeTreeNode node : tree.getNodes()){ // set the branch rate for rest of the tree
            if (! Arrays.asList(cladeRates).contains(node.getBranchRate())){
                node.setBranchRate(rootRate);
            }
        }

        // return to the tree with branch rates
        return new Value<>(null, tree, this);
    }

    // public for unit test
    public void setRate(TimeTreeNode node, double rate, boolean includeStem) {
        if (includeStem) {
            node.setBranchRate(rate);
        }
        if (node.getChildCount() == 2) {
            TimeTreeNode leftNode = node.getLeft();
            leftNode.setBranchRate(rate);
            TimeTreeNode rightNode = node.getRight();
            rightNode.setBranchRate(rate);
            if (leftNode.getChildCount() == 2) {
                setRate(leftNode, rate, true);
            }
            if (rightNode.getChildCount() == 2) {
                setRate(rightNode, rate, true);
            }
        }
    }

    public Value<TimeTree> getTree() {
        return getParams().get(treeName);
    }

    public Value<Object[]> getClades() {
        return getParams().get(cladeArrayName);
    }

    public Value<Double[]> getCladeRates() {
        return getParams().get(cladeRateArrayName);
    }

    public Value<Double> getRootRate() {
        return getParams().get(rootRateName);
    }

    public Value<Boolean> getIncludeStem() {
        return getParams().get(includeStemName);
    }
}
