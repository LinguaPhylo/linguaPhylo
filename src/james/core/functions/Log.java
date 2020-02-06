package james.core.functions;

import james.graphicalModel.*;

public class Log extends DeterministicFunction<Double> {

    final String paramName;

    public Log(@ParameterInfo(name = "x", description = "the argument.") Value<Double> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @FunctionInfo(name="log",description = "The natural logarithm function: ln x")
    public Value<Double> apply() {
        Value<Double> v = (Value<Double>)getParams().get(paramName);
        return new DoubleValue("log " + v.getId(), Math.log(v.value()), this);
    }
}
