package james.core.functions;

import james.graphicalModel.*;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by adru001 on 2/02/20.
 */
public class K80 extends DeterministicFunction<RealMatrix> {

    String paramName;

    public K80(@ParameterInfo(name = "kappa", description = "the kappa of the K80 process.") Value<Double> kappa) {
        paramName = getParamName(0);
        setParam(paramName, kappa);
    }


    @FunctionInfo(name = "k80", description = "The K80 instantaneous rate matrix. Takes a kappa and produces a K80 rate matrix.")
    public Value<RealMatrix> apply() {
        Value<Double> kappa = getParams().get(paramName);
        return new MatrixValue(getName() + "(" + kappa.getId() + ")", hky(kappa.value()), this);
    }

    private double[][] hky(double kappa) {

        int numStates = 4;

        double[][] Q = new double[numStates][numStates];

        double[] totalRates = new double[numStates];

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                if (i != j) {
                    if (Math.abs(i-j) == 2) {
                        Q[i][j] = kappa;
                    } else {
                        Q[i][j] = 1.0;
                    }
                }
                totalRates[i] += Q[i][j];
            }
            Q[i][i] = -totalRates[i];
        }

        // normalise rate matrix to one expected substitution per unit time
        double subst = 0.0;
        for (int i = 0; i < numStates; i++) {
            subst += -Q[i][i] * 0.25;
        }

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                Q[i][j] = Q[i][j] / subst;
            }
        }

        return Q;
    }
}
