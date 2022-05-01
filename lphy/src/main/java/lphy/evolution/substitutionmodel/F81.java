package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

/**
 * F81: AC = AG = AT = CG = CT = GT, unequal base frequencies, k + 3 free parameters
 * @author Alexei Drummond
 */
@Citation(value="Felsenstein, J. (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of molecular evolution, 17(6), 368-376.",
        title = "Evolutionary trees from DNA sequences: a maximum likelihood approach",
        year = 1981,
        authors = {"Felsenstein"},
        DOI="https://doi.org/10.1007/BF01734359")
public class F81 extends RateMatrix {

    public static final String freqParamName = "freq";

    public F81(@ParameterInfo(name = freqParamName, description = "the base frequencies.") Value<Double[]> freq,
               @ParameterInfo(name = meanRateParamName, description = "the mean rate of the process. default = 1.0", optional = true) Value<Number> meanRate) {

        super(meanRate);
        setParam(freqParamName, freq);
    }

    @GeneratorInfo(name = "f81", verbClause = "is", narrativeName = "F81 model",
            category = GeneratorCategory.Q_MATRIX, examples = {"f81Coalescent.lphy"},
            description = "The F81 instantaneous rate matrix. Takes base frequencies and produces an F81 rate matrix.")
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

}
