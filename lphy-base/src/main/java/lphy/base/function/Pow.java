package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.DoubleValue;

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
