package lphy.base.function.tree;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.GeneratorCategory;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.ArrayList;
import java.util.List;

import static lphy.base.evolution.EvolutionConstants.treeParamName;
import static lphy.base.evolution.tree.TimeTreeUtils.*;

/**
 * A function to return a tree pruned from a larger tree by retaining only the tips at time zero
 */
public class ExtantTree extends DeterministicFunction<TimeTree> {

    public ExtantTree(@ParameterInfo(name = treeParamName, description = "the full tree to extract extant tree from.") Value<TimeTree> tree) {
        setParam(treeParamName, tree);
    }

    @GeneratorInfo(name = "extantTree",
            category = GeneratorCategory.TREE, examples = {"simFossilsCompact.lphy"},
            description = "A tree pruned from a larger tree by retaining only the tips at time zero.")
    public Value<TimeTree> apply() {

        Value<TimeTree> tree = getParams().get(treeParamName);

        // do deep copy
        TimeTree extantTree = new TimeTree(tree.value());

        List<TimeTreeNode> sampleTips = new ArrayList<>();

        while (sampleTips.size() == 0) {
            for (TimeTreeNode node : extantTree.getNodes()) {
                if (node.isLeaf() && node.getAge() == 0.0) {
                    sampleTips.add(node);
                }
            }
        }

        for (TimeTreeNode tip : sampleTips) {
            markNodeAndDirectAncestors(tip);
        }

        removeUnmarkedNodes(extantTree);

        TimeTreeNode newRoot = getFirstNonSingleChildNode(extantTree);
        if (!newRoot.isRoot()) {
            newRoot.getParent().removeChild(newRoot);
        }

        removeSingleChildNodes(newRoot, false);

        extantTree.setRoot(newRoot, true);

        return new Value<>(null, extantTree, this);
    }
}
