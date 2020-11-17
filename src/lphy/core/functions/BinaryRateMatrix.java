package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArray2DValue;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by adru001 on 2/02/20.
 */
public class BinaryRateMatrix extends DeterministicFunction<Double[][]> {

    public static final String lambdaParamName = "lambda";

    public BinaryRateMatrix(@ParameterInfo(name = lambdaParamName, description = "the lambda parameter of the binary process. Rate of 0->1 is 1, rate of 1->0 is lambda.", type=Number.class) Value<Number> lambda) {
        setParam(lambdaParamName, lambda);
    }


    @GeneratorInfo(name = "binaryRateMatrix",
            description = "The binary trait instantaneous rate matrix. Takes a lambda and produces an instantaneous rate matrix:\n\n" +
                    "    Q = ⎡-1  1⎤\n" + // initial four spaces define a code block in markdown
                    "        ⎣ λ -λ⎦", returnType = Double[][].class)
    public Value<Double[][]> apply() {
        Value<Number> lambda = getParams().get(lambdaParamName);
        return new DoubleArray2DValue(binaryCTMC(doubleValue(lambda)), this);
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
