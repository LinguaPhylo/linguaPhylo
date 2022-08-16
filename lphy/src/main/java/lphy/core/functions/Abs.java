package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;

/**
 * @author Walter Xie
 */
public class Abs extends DeterministicFunction<Double> {

    public static final String paramName = "0";

    public Abs(@ParameterInfo(name = paramName, description = "the argument.") Value<Double> x) {
        setParam(paramName, x);
    }

    @GeneratorInfo(name="abs", description = "Return the absolute value of a number.")
    public Value<Double> apply() {
        Value<Double> v = (Value<Double>)getParams().get(paramName);
        return new DoubleValue( Math.abs(v.value()), this);
    }
}
