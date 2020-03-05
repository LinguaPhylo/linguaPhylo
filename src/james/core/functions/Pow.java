package james.core.functions;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.FunctionInfo;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.Value;
import james.graphicalModel.types.DoubleValue;

public class Pow extends DeterministicFunction<Double> {

    String bparamName;
    String xparamName;

    public Pow(@ParameterInfo(name = "0", description = "the base.") Value<Double> b,
               @ParameterInfo(name = "1", description = "the exponent.") Value<Double> x) {
        bparamName = getParamName(0);
        xparamName = getParamName(1);
        setParam(bparamName, b);
        setParam(xparamName, x);
    }

    @FunctionInfo(name = "pow", description = "The power function: b^x")
    public Value<Double> apply() {
        Value<Double> b = getParams().get(bparamName);
        Value<Double> x = getParams().get(bparamName);

        return new DoubleValue(Math.pow(b.value(), x.value()), this);
    }
}
