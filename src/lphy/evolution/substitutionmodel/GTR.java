package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.GraphicalModelNode;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArray2DValue;

/**
 * Created by adru001 on 2/02/20.
 */
public class GTR extends RateMatrix {

    public static final String ratesParamName = "rates";
    public static final String freqParamName = "freq";

    public GTR(@ParameterInfo(name = ratesParamName, narrativeName = "relative rates", description = "the relative rates of the GTR process.") Value<Double[]> rates,
               @ParameterInfo(name = freqParamName, narrativeName = "base frequencies", description = "the base frequencies.") Value<Double[]> freq,
               @ParameterInfo(name = meanRateParamName, narrativeName = "substitution rate", description = "the rate of substitution.", optional = true) Value<Number> meanRate) {

        super(meanRate);

        if (rates.value().length != 6) throw new IllegalArgumentException("Rates must have 6 dimensions.");

        setParam(ratesParamName, rates);
        setParam(freqParamName, freq);
    }

    @GeneratorInfo(name = "gtr",
            verbClause = "is assumed to be",
            narrativeName = "general time-reversible rate matrix",
            description = "The GTR instantaneous rate matrix. Takes relative rates and base frequencies and produces an GTR rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double[]> rates = getRates();
        Value<Double[]> freq = getParams().get(freqParamName);
        return new DoubleArray2DValue(gtr(rates.value(), freq.value()), this);
    }

    private Double[][] gtr(Double[] rates, Double[] freqs) {

        int numStates = 4;

        Double[][] Q = new Double[numStates][numStates];

        double[] totalRates = new double[numStates];

        // construct off-diagonals
        int upper = 0;
        for (int i = 0; i < numStates; i++) {
            for (int j = i + 1; j < numStates; j++) {
                Q[i][j] = rates[upper] * freqs[j];
                Q[j][i] = rates[upper] * freqs[i];
                upper += 1;
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
        normalize(freqs, Q, totalRateDefault1());

        return Q;
    }

    public Value<Double[]> getRates() {
        return getParams().get(ratesParamName);
    }

    public GraphicalModelNode<?> getFreq() {
        return getParams().get(freqParamName);
    }
}
