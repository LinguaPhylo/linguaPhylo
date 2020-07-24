package lphy.core.functions;

import beast.core.BEASTInterface;
import lphy.evolution.coalescent.StructuredCoalescent;
import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.IntegerValue;

import java.util.Map;

public class MigrationCount extends DeterministicFunction<Integer> {

    final String paramName;

    public MigrationCount(@ParameterInfo(name = "tree", description = "the tree.") Value<TimeTree> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="countMigrations",
            description = "The number of single-child nodes in the tree where the '" +
                    StructuredCoalescent.populationLabel + "' attribute changes.")
    public Value<Integer> apply() {
        Value<TimeTree> v = (Value<TimeTree>)getParams().get(paramName);
        return new IntegerValue(StructuredCoalescent.countMigrations(v.value()), this);
    }
}
