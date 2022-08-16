package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;

public class Ceil extends DeterministicFunction<Integer> {

    public static final String paramName = "0";

    public Ceil(@ParameterInfo(name = paramName, description = "the argument.") Value<Double> x) {
        setParam(paramName, x);
    }

    @GeneratorInfo(name="ceil",description = "The ceil function.")
    public Value<Integer> apply() {
        Value<Double> v = (Value<Double>)getParams().get(paramName);
        return new IntegerValue((int)Math.ceil(v.value()), this);
    }
}
