package lphy.base.evolution.branchrate;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.Arrays;

public class LocalClock extends DeterministicFunction<Double[]> {
    public static final String treeName = "tree";
    public static final String cladeArrayName = "clades";
    public static final String cladeRateArrayName = "cladeRates";
    public static final String rootRateName = "rootRate";

    public static final String includeStemName = "includeStem";

    // TODO: default is to include stem, add option for includeStem=false
    public LocalClock(
            @ParameterInfo(name = treeName, description = "the tree used to calculate branch rates" ) Value<TimeTree> tree,
            @ParameterInfo(name = cladeArrayName, description = "the array of the node names") Value<TimeTreeNode[]> clades,
            @ParameterInfo(name = cladeRateArrayName, description = "the array of clade rates") Value<Double[]> cladeRates,
            @ParameterInfo(name = rootRateName, description = "the root rate of the tree") Value<Double> rootRate,
            @ParameterInfo(name = includeStemName, description = "whether to include stem of clades, defaults to true", optional = true) Value<Boolean> includeStem
            ){
        if (tree == null) throw new IllegalArgumentException("The tree can't be null!");
        if (clades == null) throw new IllegalArgumentException("The clades can't be null!");
        if (cladeRates == null) throw new IllegalArgumentException("The clade rates can't be null!");
        if (rootRate == null) throw new IllegalArgumentException("The root rate can't be null!");
        setParam(treeName, tree);
        setParam(cladeArrayName, clades);
        setParam(cladeRateArrayName, cladeRates);
        setParam(rootRateName, rootRate);
        setParam(includeStemName, includeStem);
    }

    @GeneratorInfo(name = "localClock", description = "Apply local clock in a phylogenetic tree to generate the " +
            "branch rates. The order of elements in clades and cladeRates array should match. The clades" +
            " should not be overlapped with each other.")
    @Override
    public Value<Double[]> apply() {
        Value<TimeTree> tree = getParams().get(treeName);
        Value<TimeTreeNode[]> clades = getParams().get(cladeArrayName);
        Value<Double[]> cladeRates = getParams().get(cladeRateArrayName);
        Value<Double> rootRate = getParams().get(rootRateName);
        Value<Boolean> includeStem = getParams().get(includeStemName);

        // set the rates within specified clades
        for (int i = 0; i < clades.value().length; i++){
            TimeTreeNode clade = clades.value()[i];
            double rate = cladeRates.value()[i];
            if (includeStem == null || includeStem.value()) {
                setRate(clade, rate, true);
            } else {
                setRate(clade, rate, false);
            }
        }

        // initialise the branch rate array
        Double[] branchRates = new Double[tree.value().branchCount()];

        for (TimeTreeNode node : tree.value().getNodes()){
            // set the branch rate for rest of the tree
            if (! Arrays.asList(cladeRates.value()).contains(node.getBranchRate())){
                node.setBranchRate(rootRate.value());
            }

            if (! node.isRoot()) {
                // write the branch rate into the array
                int cladeNumber = node.getIndex();
                branchRates[cladeNumber] = node.getBranchRate();
            }
        }

        // return to the branch rate list
        return new Value<>(null, branchRates, this);
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

    public static Double getCladeRate(TimeTreeNode clade, Value<TimeTreeNode[]> clades, Value<Double[]> cladeRates) {
        for (int i = 0; i< clades.value().length; i++){
            if (clade == clades.value()[i]){
                return cladeRates.value()[i];
            }
        }
        return null;
    }
}
