package james.core.functions;

import james.graphicalModel.*;
import james.graphicalModel.types.DoubleArray2DValue;

/**
 * Created by adru001 on 2/02/20.
 */
public class GTR extends RateMatrix {

    String rateParamName;
    String freqParamName;

    public GTR(@ParameterInfo(name = "rates", description = "the relative rates of the GTR process.") Value<Double[]> rates, @ParameterInfo(name = "freq", description = "the base frequencies.") Value<Double[]> freq) {
        rateParamName = getParamName(0);
        freqParamName = getParamName(1);

        if (rates.value().length != 6) throw new IllegalArgumentException("Rates must have 6 dimensions.");

        setParam(rateParamName, rates);
        setParam(freqParamName, freq);
    }


    @FunctionInfo(name = "gtr", description = "The GTR instantaneous rate matrix. Takes relative rates and base frequencies and produces an GTR rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double[]> rates = getParams().get(rateParamName);
        Value<Double[]> freq = getParams().get(freqParamName);
        return new DoubleArray2DValue(getName() + "(" + rates.getId() + ", " + freq.getId() + ")", gtr(rates.value(), freq.value()), this);
    }

    private Double[][] gtr(Double[] rates, Double[] freqs) {

        int numStates = 4;
        
        Double[][] Q = new Double[numStates][numStates];

        double[] totalRates = new double[numStates];

        // construct off-diagonals
        int upper = 0;
        for (int i = 0; i < numStates; i++) {
            for (int j = i; j < numStates; j++) {
                if (j > i) {
                    Q[i][j] = rates[upper] * freqs[j];
                    Q[j][i] = rates[upper] * freqs[i];
                    upper += 1;
                }
            }
        }

        // construct diagonals
        for (int i = 0; i < numStates; i++) {
            double totalRate = 0.0;
            for (int j = 0; j < numStates; j++) {
                if (j != i) {
                    totalRate += Q[i][j];
                }
            }
            Q[i][i] = -totalRate;
        }
        // normalise rate matrix to one expected substitution per unit time
        normalize(freqs, Q);

        return Q;
    }
}
