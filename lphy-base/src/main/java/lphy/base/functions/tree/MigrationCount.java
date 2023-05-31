package lphy.base.functions.tree;

import lphy.base.evolution.coalescent.StructuredCoalescent;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.graphicalmodel.components.*;
import lphy.core.graphicalmodel.types.IntegerValue;

public class MigrationCount extends DeterministicFunction<Integer> {

    public static final String treeParamName = "tree";

    public MigrationCount(@ParameterInfo(name = treeParamName, description = "the tree.") Value<TimeTree> x) {
        setParam(treeParamName, x);
    }

    @GeneratorInfo(name="countMigrations",
            category = GeneratorCategory.TREE, examples = {"simpleStructuredCoalescent.lphy"},
            description = "The number of single-child nodes in the tree where the '" +
                    StructuredCoalescent.populationLabel + "' attribute changes.")
    public Value<Integer> apply() {
        Value<TimeTree> v = (Value<TimeTree>)getParams().get(treeParamName);
        return new IntegerValue(StructuredCoalescent.countMigrations(v.value()), this);
    }
}
