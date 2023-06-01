package lphy.base.evolution.branchrates;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.components.*;

import java.util.Map;

public class LocalBranchRates extends DeterministicFunction<Double[]> {

    public static final String treeParamName = "tree";
    public static final String indicatorsParamName = "indicators";
    public static final String ratesParamName = "rates";

    public LocalBranchRates(@ParameterInfo(name = treeParamName, description = "the tree.") Value<TimeTree> tree,
                            @ParameterInfo(name = indicatorsParamName, description = "a boolean indicator for each node except the root. " +
                                    "True if there is a new rate on the branch above this node, false if the rate is inherited from the parent node.") Value<Boolean[]> indicators,
                            @ParameterInfo(name = ratesParamName, description = "A rate for each node in the tree (except root). " +
                                    "Only those with a corresponding indicator are used.") Value<Double[]> rates) {
        setParam(treeParamName, tree);
        setParam(indicatorsParamName, indicators);
        setParam(ratesParamName, rates);
    }

    @GeneratorInfo(name = "localBranchRates",
            category = GeneratorCategory.TREE, examples = {"simpleRandomLocalClock.lphy","simpleRandomLocalClock2.lphy"},
            description = "A function that returns branch rates for the given tree, " +
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

    public Value<TimeTree> getTree() {
        return getParams().get(treeParamName);
    }

    public Value<Double[]> getRates() {
        return getParams().get(ratesParamName);
    }

    public Value<Boolean[]> getIndicators() {
        return getParams().get(indicatorsParamName);
    }
}
