package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.FunctionInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArray2DValue;

/**
 * Created by adru001 on 2/02/20.
 */
public class BinaryRateMatrix extends DeterministicFunction<Double[][]> {

    String paramName;

    public BinaryRateMatrix(@ParameterInfo(name = "lambda", description = "the lambda parameter of the binary process. Rate of 0->1 is 1, rate of 1->0 is lambda.") Value<Double> kappa) {
        paramName = getParamName(0);
        setParam(paramName, kappa);
    }


    @FunctionInfo(name = "binaryRateMatrix",
            description = "The binary trait instantaneous rate matrix. Takes a lambda and produces an instantaneous rate matrix:<br><br><pre>\n" +
                    "  Q = ⎡-1  1⎤\n" +
                    "      ⎣ λ -λ⎦\n" +
                    "</pre>")
    public Value<Double[][]> apply() {
        Value<Double> lambda = getParams().get(paramName);
        return new DoubleArray2DValue(binaryCTMC(lambda.value()), this);
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
