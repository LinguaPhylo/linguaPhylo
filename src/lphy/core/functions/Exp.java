package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleValue;

public class Exp extends DeterministicFunction<Double> {

    String paramName;

    public Exp(@ParameterInfo(name = "0", description = "the argument.") Value<Double> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @FunctionInfo(name="exp",description = "The exponential function: e^x")
    public Value<Double> apply(Value<Double> v) {
        setParam("0", v);
        return new DoubleValue(Math.exp(v.value()), this);
    }

    @Override
    public Value<Double> apply() {
        return apply((Value<Double>)getParams().get(paramName));
    }
}
