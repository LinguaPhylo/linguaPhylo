package james.core.functions;

import james.graphicalModel.*;
import james.graphicalModel.types.DoubleArray2DValue;
import james.graphicalModel.types.MatrixValue;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by adru001 on 2/02/20.
 */
public class K80 extends DeterministicFunction<Double[][]> {

    String paramName;

    public K80(@ParameterInfo(name = "kappa", description = "the kappa of the K80 process.") Value<Double> kappa) {
        paramName = getParamName(0);
        setParam(paramName, kappa);
    }


    @FunctionInfo(name = "k80", description = "The K80 instantaneous rate matrix. Takes a kappa and produces a K80 rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double> kappa = getParams().get(paramName);
        return new DoubleArray2DValue(getName() + "(" + kappa.getId() + ")", k80(kappa.value()), this);
    }

    private Double[][] k80(double kappa) {

        int numStates = 4;

        Double[][] Q = new Double[numStates][numStates];

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
