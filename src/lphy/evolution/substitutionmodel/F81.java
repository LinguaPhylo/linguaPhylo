package lphy.evolution.substitutionmodel;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy.beast.BEASTContext;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArray2DValue;

import java.util.Map;

/**
 * Created by adru001 on 2/02/20.
 */
public class F81 extends RateMatrix {

    String freqParamName;

    public F81(@ParameterInfo(name = "freq", description = "the base frequencies.") Value<Double[]> freq) {
        freqParamName = getParamName(0);
        setParam(freqParamName, freq);
    }


    @GeneratorInfo(name = "f81", description = "The F81 instantaneous rate matrix. Takes base frequencies and produces an F81 rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double[]> freq = getFreq();
        return new DoubleArray2DValue(f81(freq.value()), this);
    }

    private Double[][] f81(Double[] freqs) {

        int numStates = 4;

        Double[][] Q = new Double[numStates][numStates];

        double[] totalRates = new double[numStates];

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                if (i != j) {
                    Q[i][j] = freqs[j];
                } else Q[i][i] = 0.0;
                totalRates[i] += Q[i][j];
            }
            Q[i][i] = -totalRates[i];
        }

        // normalise rate matrix to one expected substitution per unit time
        normalize(freqs, Q);

        return Q;
    }

    public Value<Double[]> getFreq() {
        return getParams().get(freqParamName);
    }

    @Override
    public BEASTInterface toBEAST(BEASTInterface value, Map beastObjects) {
        beast.evolution.substitutionmodel.HKY beastF81 = new beast.evolution.substitutionmodel.HKY();
        beastF81.setInputValue("kappa", new RealParameter("1.0"));
        beastF81.setInputValue("frequencies", BEASTContext.createBEASTFrequencies((RealParameter) beastObjects.get(getFreq())));
        beastF81.initAndValidate();
        return beastF81;
    }
}
