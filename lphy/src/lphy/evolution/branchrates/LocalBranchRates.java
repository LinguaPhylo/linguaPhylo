package lphy.evolution.branchrates;

import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import java.util.Map;

public class LocalBranchRates extends DeterministicFunction<Double[]> {

    final String treeParamName;
    final String indicatorsParamName;
    final String ratesParamName;

    public LocalBranchRates(@ParameterInfo(name = "tree", description = "the tree in Newick format.") Value<TimeTree> tree,
                            @ParameterInfo(name = "indicators", description = "a boolean indicator for each node except the root. " +
                                    "True if there is a new rate on the branch above this node, false if the rate is inherited from the parent node.") Value<Boolean[]> indicators,
                            @ParameterInfo(name = "rates", description = "A rate for each node in the tree (except root). " +
                                    "Only those with a corresponding indicator are used.") Value<TimeTree> rates) {
        treeParamName = getParamName(0);
        indicatorsParamName = getParamName(1);
        ratesParamName = getParamName(2);
        setParam(treeParamName, tree);
        setParam(indicatorsParamName, indicators);
        setParam(ratesParamName, rates);
    }

    @GeneratorInfo(name = "localBranchRates", description = "A function that returns branch rates for the given tree, " +
            "indicator mask and raw rates. Each branch takes on the rate of its node index if the indicator is true, " +
            "or inherits the rate of its parent branch otherwise.")
    public Value<Double[]> apply() {

        Map<String, Value> params = getParams();

        Double[] rawRates = ((Value<Double[]>) params.get(ratesParamName)).value();
        Boolean[] indicators = ((Value<Boolean[]>) params.get(indicatorsParamName)).value();
        TimeTree tree = ((Value<TimeTree>) params.get(treeParamName)).value();

        Double[] branchRates = new Double[rawRates.length];
        traverseTree(tree.getRoot(), branchRates, rawRates, indicators);

        return new Value<>(branchRates, this);
    }

    private void traverseTree(TimeTreeNode node, Double[] branchRates, Double[] rawRates, Boolean[] indicators) {

        int nodeNumber = node.getIndex();

        // if this is the root or the indicator is true then take the raw rate as this branch's rate;
        if (node.isRoot() || indicators[nodeNumber]) {
            branchRates[nodeNumber] = rawRates[nodeNumber];
        } else {   // if indicator is false then take branchRate of parent. Recursion is parent-first, so this will always have been populated before this call.
            branchRates[nodeNumber] = branchRates[node.getParent().getIndex()];
        }

        for (TimeTreeNode child : node.getChildren()) {
            traverseTree(child, branchRates, rawRates, indicators);
        }
    }
}
