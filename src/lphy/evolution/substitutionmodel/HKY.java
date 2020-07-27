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
public class HKY extends RateMatrix {

    String kappaParamName;
    String freqParamName;

    public HKY(@ParameterInfo(name = "kappa", description = "the kappa of the HKY process.") Value<Double> kappa, @ParameterInfo(name = "freq", description = "the base frequencies.") Value<Double[]> freq) {
        kappaParamName = getParamName(0);
        freqParamName = getParamName(1);
        setParam(kappaParamName, kappa);
        setParam(freqParamName, freq);
    }


    @GeneratorInfo(name = "hky", description = "The HKY instantaneous rate matrix. Takes a kappa and base frequencies and produces an HKY85 rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double> kappa = getKappa();
        Value<Double[]> freq = getFreq();
        return new DoubleArray2DValue(hky(kappa.value(), freq.value()), this);
    }

    public Value<Double> getKappa() {
        return getParams().get(kappaParamName);
    }

    public Value<Double[]> getFreq() {
        return getParams().get(freqParamName);
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

    public BEASTInterface toBEAST(BEASTInterface value, BEASTContext context) {
        beast.evolution.substitutionmodel.HKY beastHKY = new beast.evolution.substitutionmodel.HKY();
        beastHKY.setInputValue("kappa", context.getBEASTObject(getKappa()));
        beastHKY.setInputValue("frequencies", BEASTContext.createBEASTFrequencies((RealParameter) context.getBEASTObject(getFreq())));
        beastHKY.initAndValidate();
        return beastHKY;
    }
}
