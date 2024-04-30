package lphy.base.evolution.branchrate;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.Arrays;
import java.util.Map;

public class LocalClock extends DeterministicFunction<Double[]> {
    public static final String treeName = "tree";
    public static final String cladeArrayName = "clades";
    public static final String cladeRateArrayName = "cladeRates";
    public static final String rootRateName = "rootRate";

    public static final String includeStemName = "includeStem";

    public static final boolean DEFAULT_INCLUDE_STEM = true;

    // TODO: add option for includeStem=[false]
    public LocalClock(
            @ParameterInfo(name = treeName, description = "the tree used to calculate branch rates" ) Value<TimeTree> tree,
            @ParameterInfo(name = cladeArrayName, description = "the array of the node names") Value<Object[]> clades,
            @ParameterInfo(name = cladeRateArrayName, description = "the array of clade rates") Value<Double[]> cladeRates,
            @ParameterInfo(name = rootRateName, description = "the root rate of the tree") Value<Double> rootRate,
            @ParameterInfo(name = includeStemName, description = "whether to include stem of clades, defaults to true", optional = true) Value<Boolean> includeStem){
        if (tree == null) throw new IllegalArgumentException("The tree can't be null!");
        if (clades == null) throw new IllegalArgumentException("The clades can't be null!");
        if (cladeRates == null) throw new IllegalArgumentException("The clade rates can't be null!");
        if (rootRate == null) throw new IllegalArgumentException("The root rate can't be null!");
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

    @GeneratorInfo(name = "localClock", description = "Apply local clock in a phylogenetic tree to generate the " +
            "branch rates. The order of elements in clades and cladeRates array should match. The clades" +
            " should not be overlapped with each other.")
    @Override
    public Value<Double[]> apply() {
        Map<String, Value> params = getParams();
        // get parameters
        TimeTree tree = ((Value<TimeTree>)params.get(treeName)).value();
        Object[] clades = ((Value<Object[]>)params.get(cladeArrayName)).value();
        Double[] cladeRates = ((Value<Double[]>)params.get(cladeRateArrayName)).value();
        Double rootRate = ((Value<Double>)params.get(rootRateName)).value();
        Boolean includeStem = ((Value<Boolean>)params.get(includeStemName)).value();

        // set the rates within specified clades
        for (int i = 0; i < clades.length; i++){
            TimeTreeNode clade = (TimeTreeNode) clades[i];
            double rate = cladeRates[i];
            if (includeStem == null || includeStem) {
                setRate(clade, rate, true);
            } else {
                setRate(clade, rate, false);
            }
        }

        // initialise the branch rate array
        Double[] branchRates = new Double[tree.branchCount()];

        for (TimeTreeNode node : tree.getNodes()){ // set the branch rate for rest of the tree
            if (! Arrays.asList(cladeRates).contains(node.getBranchRate())){
                node.setBranchRate(rootRate);
            }

            if (! node.isRoot()) { // write the branch rate into the array
                int cladeNumber = node.getIndex();
                branchRates[cladeNumber] = node.getBranchRate();
            }
        }

        // return to the branch rate list
        return new Value<>(branchRates, this);
    }

    // public for unit test
    public void setRate(TimeTreeNode node, double rate, boolean includeNode) {
        if (includeNode) {
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
