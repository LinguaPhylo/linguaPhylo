package lphy.core.functions.tree;

import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;

import static lphy.evolution.EvolutionConstants.treeParamName;
import static lphy.evolution.tree.TimeTreeUtils.*;

/**
 * A function to return a tree pruned from a larger tree by retaining only the tips at time zero
 */
public class PruneTree extends DeterministicFunction<TimeTree> {

    public PruneTree(@ParameterInfo(name = treeParamName, description = "the full tree to sample") Value<TimeTree> tree) {
        setParam(treeParamName, tree);
    }

    @GeneratorInfo(name = "pruneTree",
            category = GeneratorCategory.TREE, examples = {"simFossils.lphy"},
            description = "A tree pruned from a larger tree by retaining only nodes subtending nodes with non-null id's.")
    public Value<TimeTree> apply() {

        Value<TimeTree> tree = getParams().get(treeParamName);

        // do deep copy
        TimeTree prunedTree = new TimeTree(tree.value());

        for (TimeTreeNode node : prunedTree.getNodes()) {
            if (node.getId() != null) {
                markNodeAndDirectAncestors(node);
            }
        }

        removeUnmarkedNodes(prunedTree);

        removeSingleChildNodes(prunedTree, true);

        prunedTree.setRoot(prunedTree.getRoot(),true);

        removeMarks(prunedTree);

        return new Value<>(null, prunedTree, this);
    }
}
