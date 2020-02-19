package james.core.functions;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.FunctionInfo;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.Value;
import james.graphicalModel.types.DoubleArray2DValue;

/**
 * Created by adru001 on 2/02/20.
 */
public class BinaryRateMatrix extends DeterministicFunction<Double[][]> {

    String paramName;

    public BinaryRateMatrix(@ParameterInfo(name = "lambda", description = "the lambda parameter of the binary process. Rate of 0->1 is 1, rate of 1->0 is lambda.") Value<Double> kappa) {
        paramName = getParamName(0);
        setParam(paramName, kappa);
    }


    @FunctionInfo(name = "binaryRateMatrix", description = "The binary trait instantaneous rate matrix. Takes a lambda and produces a instantaneous rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double> lambda = getParams().get(paramName);
        return new DoubleArray2DValue(getName() + "(" + lambda.getId() + ")", binaryCTMC(lambda.value()), this);
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
