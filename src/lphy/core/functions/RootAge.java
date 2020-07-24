package lphy.core.functions;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;

public class RootAge extends DeterministicFunction<Double> {

    final String paramName;

    public RootAge(@ParameterInfo(name = "tree", description = "the tree.") Value<TimeTree> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="rootAge",description = "The age of the root of the tree.")
    public Value<Double> apply() {
        Value<TimeTree> v = (Value<TimeTree>)getParams().get(paramName);
        return new DoubleValue(v.value().getRoot().getAge(), this);
    }
}
