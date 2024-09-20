package lphy.base.function.tree;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import static lphy.base.evolution.EvolutionConstants.treeParamName;

/**
 * A function to set internal nodes id given a tree
 */
public class InternalNodesID extends DeterministicFunction<TimeTree> {

    public InternalNodesID(@ParameterInfo(name = treeParamName,
            description = "the tree to set internal nodes id.") Value<TimeTree> tree) {
        setParam(treeParamName, tree);
    }

    @GeneratorInfo(name = "setInternalNodesID", category = GeneratorCategory.TREE,
            description = "set internal nodes id given a tree.")
    public Value<TimeTree> apply() {

        Value<TimeTree> tree = getParams().get(treeParamName);

        // do deep copy
        TimeTree newTree = new TimeTree(tree.value());

        for (TimeTreeNode node : newTree.getInternalNodes()) {
            if (node.getId() == null) // set index as id
                node.setId(String.valueOf(node.getIndex()));
        }

        return new Value<>(null, newTree, this);
    }
}