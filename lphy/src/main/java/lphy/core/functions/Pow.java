package lphy.core.functions;

import lphy.core.ParameterNames;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;

public class Pow extends DeterministicFunction<Double> {

    final String bparamName = ParameterNames.NoParamName0;
    final String xparamName = ParameterNames.NoParamName1;

    public Pow(@ParameterInfo(name = bparamName, description = "the base.") Value<Double> b,
               @ParameterInfo(name = xparamName, description = "the exponent.") Value<Double> x) {
        // this adds value to output, so no arg name works when click sample button
        setInput(bparamName, b);
        setInput(xparamName, x);
    }

    // This cannot be built-in func, because ExpressionNode1Arg is only parsing 1-arg Function,
    // and ExpressionNode2Args implemented is for BinaryOperator (not BiFunction).
    @GeneratorInfo(name = "pow", description = "The power function: b^x")
    public Value<Double> apply() {
        Value<Double> b = getParams().get(bparamName);
        Value<Double> x = getParams().get(xparamName);

        return new DoubleValue(Math.pow(b.value(), x.value()), this);
    }
}
