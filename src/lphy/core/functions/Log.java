package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleValue;

public class Log extends DeterministicFunction<Double> {

    final String paramName;

    public Log(@ParameterInfo(name = "0", description = "the argument.") Value<Double> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @FunctionInfo(name="log",description = "The natural logarithm function: ln x")
    public Value<Double> apply() {
        Value<Double> v = (Value<Double>)getParams().get(paramName);
        return new DoubleValue( Math.log(v.value()), this);
    }
}
