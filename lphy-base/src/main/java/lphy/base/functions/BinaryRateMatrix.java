package lphy.base.functions;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.GeneratorCategory;
import lphy.core.model.components.Value;
import lphy.core.model.components.ValueUtils;
import lphy.core.model.types.DoubleArray2DValue;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
public class BinaryRateMatrix extends DeterministicFunction<Double[][]> {

    public static final String lambdaParamName = "lambda";

    public BinaryRateMatrix(@ParameterInfo(name = lambdaParamName, description = "the lambda parameter of the binary process. Rate of 0->1 is 1, rate of 1->0 is lambda.") Value<Number> lambda) {
        setParam(lambdaParamName, lambda);
    }


    @GeneratorInfo(name = "binaryRateMatrix",
            category = GeneratorCategory.RATE_MATRIX, examples = {"errorModel1.lphy", "errorModel2.lphy"},
            description = "The binary trait instantaneous rate matrix. Takes a lambda and produces an instantaneous rate matrix:\n\n" +
                    "    Q = ⎡-1  1⎤\n" + // initial four spaces define a code block in markdown
                    "        ⎣ λ -λ⎦")
    public Value<Double[][]> apply() {
        Value<Number> lambda = getParams().get(lambdaParamName);
        return new DoubleArray2DValue(binaryCTMC(ValueUtils.doubleValue(lambda)), this);
    }

    private Double[][] binaryCTMC(double lambda) {

        Double[][] Q = {{-1.0, 1.0}, {lambda, -lambda}};

        double[] freqs = {lambda / (lambda + 1.0), 1.0 / (lambda + 1.0)};

        double[] rate = {1, lambda};

        double subst = rate[0] * freqs[0] + rate[1] * freqs[1];

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                Q[i][j] = Q[i][j] / subst;
            }
        }

        return Q;
    }
}
