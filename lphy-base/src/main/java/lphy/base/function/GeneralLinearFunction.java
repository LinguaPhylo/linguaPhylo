package lphy.base.function;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.Value;
import lphy.core.model.datatype.DoubleValue;

public class GeneralLinearFunction extends DeterministicFunction<Double> {

    public static final String betaParamName = "beta";
    public static final String xParamName = "x";

    public GeneralLinearFunction(@ParameterInfo(name = betaParamName, description = "the coefficients of the explanatory variable x.") Value<Double[]> b,
                                 @ParameterInfo(name = xParamName, description = "the explanatory variable x.") Value<Double[]> x) {
        setParam(betaParamName, b);
        setParam(xParamName, x);
    }

    @GeneratorInfo(name = "generalLinearFunction", description = "The general linear function: y = \\sum_i b_i*x_i")
    public Value<Double> apply() {
        Value<Double[]> b = getParams().get(betaParamName);
        Value<Double[]> x = getParams().get(xParamName);

        double y = 0.0;
        for (int i = 0; i < b.value().length; i++) {
            y += b.value()[i] * x.value()[i];
        }

        return new DoubleValue(y, this);
    }
}
