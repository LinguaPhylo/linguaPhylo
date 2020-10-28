package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleValue;

import static lphy.graphicalModel.ValueUtils.doubleValue;

public class Exp extends DeterministicFunction<Double> {

    public static final String argParamName = "arg";

    public Exp(@ParameterInfo(name = argParamName, description = "the argument.") Value<Number> x) {
        setParam(argParamName, x);
    }

    public Value<Double> apply(Value<Number> v) {
        setParam(argParamName, v);
        return new DoubleValue(null, Math.exp(doubleValue(v)), this);
    }

    @GeneratorInfo(name="exp",description = "The exponential function: e^x")
    public Value<Double> apply() {
        return apply((Value<Number>)getParams().get(argParamName));
    }
}
