package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.IntegerValue;

public class Floor extends DeterministicFunction<Integer> {

    public static final String paramName = "0";

    public Floor(@ParameterInfo(name = paramName, description = "the argument.") Value<Double> x) {
        setParam(paramName, x);
    }

    @GeneratorInfo(name="floor",description = "The floor function.")
    public Value<Integer> apply() {
        Value<Double> v = (Value<Double>)getParams().get(paramName);
        return new IntegerValue((int)Math.floor(v.value()), this);
    }
}
