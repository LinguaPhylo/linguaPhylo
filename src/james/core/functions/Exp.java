package james.core.functions;

import james.graphicalModel.*;
import james.graphicalModel.types.DoubleValue;

public class Exp extends DeterministicFunction<Double> {

    String paramName;

    public Exp(@ParameterInfo(name = "x", description = "the argument.") Value<Double> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @FunctionInfo(name="exp",description = "The exponential function: e^x")
    public Value<Double> apply(Value<Double> v) {
        setParam("x", v);
        return new DoubleValue("exp(" + v.getId() + ")", Math.exp(v.value()), this);
    }

    @Override
    public Value<Double> apply() {
        return apply((Value<Double>)getParams().get(paramName));
    }
}
