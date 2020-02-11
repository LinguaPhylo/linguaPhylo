package james.core.functions;

import james.graphicalModel.*;
import james.graphicalModel.types.MatrixValue;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.List;

/**
 * Created by adru001 on 2/02/20.
 */
public class GTR extends DeterministicFunction<RealMatrix> {

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
    public Value<RealMatrix> apply() {
        Value<Double[]> rates = getParams().get(rateParamName);
        Value<Double[]> freq = getParams().get(freqParamName);
        return new MatrixValue(getName() + "(" + rates.getId() + ", " + freq.getId() + ")", gtr(rates.value(), freq.value()), this);
    }

    private double[][] gtr(Double[] rates, Double[] freqs) {

        int numStates = 4;
        
        double[][] Q = new double[numStates][numStates];

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
        double subst = 0.0;
        for (int i = 0; i < numStates; i++) {
            subst += -Q[i][i] * freqs[i];
        }

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                Q[i][j] = Q[i][j] / subst;
            }
        }

        return Q;
    }
}
