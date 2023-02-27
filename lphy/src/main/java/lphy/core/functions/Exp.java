package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;
import lphy.parser.functions.ExpressionNode1Arg;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Already exists in lphy.parser.SimulatorListenerImpl
 * ValueOrFunction#visitMethodCall(SimulatorParser.MethodCallContext)
 * Use built-in function {@link ExpressionNode1Arg#exp()}
 */
@Deprecated
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
