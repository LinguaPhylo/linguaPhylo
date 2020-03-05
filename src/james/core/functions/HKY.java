package james.core.functions;

import james.graphicalModel.*;
import james.graphicalModel.types.DoubleArray2DValue;

/**
 * Created by adru001 on 2/02/20.
 */
public class HKY extends RateMatrix {

    String kappaParamName;
    String freqParamName;

    public HKY(@ParameterInfo(name = "kappa", description = "the kappa of the HKY process.") Value<Double> kappa, @ParameterInfo(name = "freq", description = "the base frequencies.") Value<Double[]> freq) {
        kappaParamName = getParamName(0);
        freqParamName = getParamName(1);
        setParam(kappaParamName, kappa);
        setParam(freqParamName, freq);
    }


    @FunctionInfo(name = "hky", description = "The HKY instantaneous rate matrix. Takes a kappa and base frequencies and produces an HKY85 rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double> kappa = getParams().get(kappaParamName);
        Value<Double[]> freq = getParams().get(freqParamName);
        return new DoubleArray2DValue(hky(kappa.value(), freq.value()), this);
    }

    private Double[][] hky(double kappa, Double[] freqs) {

        int numStates = 4;
        
        Double[][] Q = new Double[numStates][numStates];

        double[] totalRates = new double[numStates];

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                if (i != j) {
                    if (Math.abs(i-j) == 2) {
                        Q[i][j] = kappa * freqs[j];
                    } else {
                        Q[i][j] = freqs[j];
                    }
                } else Q[i][i] = 0.0;
                totalRates[i] += Q[i][j];
            }
            Q[i][i] = -totalRates[i];
        }

        // normalise rate matrix to one expected substitution per unit time
        normalize(freqs, Q);

        return Q;
    }
}
