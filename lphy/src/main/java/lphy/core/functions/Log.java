package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;

import static lphy.core.ParameterNames.NoParamName0;

public class Log extends DeterministicFunction<Double> {

    final String paramName = NoParamName0;

    public Log(@ParameterInfo(name = paramName, description = "the argument.") Value<Double> x) {
        setParam(paramName, x);
    }

    @GeneratorInfo(name="log",description = "The natural logarithm function: ln x")
    public Value<Double> apply() {
        Value<Double> v = (Value<Double>)getParams().get(paramName);
        return new DoubleValue( Math.log(v.value()), this);
    }
}
