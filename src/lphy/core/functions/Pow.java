package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;

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

    @GeneratorInfo(name = "pow", description = "The power function: b^x")
    public Value<Double> apply() {
        Value<Double> b = getParams().get(bparamName);
        Value<Double> x = getParams().get(bparamName);

        return new DoubleValue(Math.pow(b.value(), x.value()), this);
    }
}
