package lphy.evolution.substitutionmodel;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy.beast.BEASTContext;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

import java.util.Map;

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

    @GeneratorInfo(name = "gtr", description = "The GTR instantaneous rate matrix. Takes relative rates and base frequencies and produces an GTR rate matrix.")
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
        normalize(freqs, Q);

        return Q;
    }

    public Value<Double[]> getRates() {
        return getParams().get(rateParamName);
    }

    public GraphicalModelNode<?> getFreq() {
        return getParams().get(freqParamName);
    }

    public BEASTInterface toBEAST(BEASTInterface value, BEASTContext context) {

        substmodels.nucleotide.GTR beastGTR = new substmodels.nucleotide.GTR();

        Value<Double[]> rates = getRates();

        beastGTR.setInputValue("rates", context.getBEASTObject(rates));
        beastGTR.setInputValue("frequencies", BEASTContext.createBEASTFrequencies((RealParameter) context.getBEASTObject(getFreq())));
        beastGTR.initAndValidate();
        return beastGTR;
    }
}
