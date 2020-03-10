package lphy.core.functions;

import lphy.StructuredCoalescent;
import lphy.TimeTree;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;

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
