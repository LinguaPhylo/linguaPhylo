package lphy.base.function.tree;

import lphy.base.evolution.coalescent.StructuredCoalescent;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.model.component.Value;
import lphy.core.model.component.argument.ParameterInfo;
import lphy.core.model.datatype.IntegerValue;

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
