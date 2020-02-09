package james.core.functions;

import james.graphicalModel.*;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.List;

/**
 * Created by adru001 on 2/02/20.
 */
public class HKY extends DeterministicFunction<RealMatrix> {

    String kappaParamName;
    String freqParamName;

    public HKY(@ParameterInfo(name = "kappa", description = "the kappa of the HKY process.") Value<Double> kappa, @ParameterInfo(name = "freq", description = "the base frequencies.") Value<List<Double>> freq) {
        kappaParamName = getParamName(0);
        freqParamName = getParamName(1);
        setParam(kappaParamName, kappa);
        setParam(freqParamName, freq);
    }


    @FunctionInfo(name = "hky", description = "The HKY instantaneous rate matrix. Takes a kappa and base frequencies and produces an HKY85 rate matrix.")
    public Value<RealMatrix> apply() {
        Value<Double> kappa = getParams().get(kappaParamName);
        Value<List<Double>> freq = getParams().get(freqParamName);
        return new MatrixValue(getName() + "(" + kappa.getId() + ")", hky(kappa.value(), freq.value()), this);
    }

    private double[][] hky(double kappa, List<Double> freqs) {

        int numStates = 4;
        
        double[][] Q = new double[numStates][numStates];

        double[] totalRates = new double[numStates];

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                if (i != j) {
                    if (Math.abs(i-j) == 2) {
                        Q[i][j] = kappa * freqs.get(j);
                    } else {
                        Q[i][j] = freqs.get(j);
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
