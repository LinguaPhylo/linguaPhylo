package lphy.base.evolution.substitutionmodel;

import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.components.GeneratorCategory;
import lphy.core.model.components.Value;
import lphy.core.model.types.DoubleArray2DValue;

/**
 * GTR: AC, AG, AT, CG, CT, GT, unequal base frequencies, k + 8 free parameters
 * @author Alexei Drummond
 */
@Citation(value="Tavaré, S. (1986). Some probabilistic and statistical problems in the analysis of DNA sequences. " +
        "Lectures on mathematics in the life sciences, 17(2), 57-86.",
        title = "Some probabilistic and statistical problems in the analysis of DNA sequences",
        authors = {"Tavaré"}, year=1986, ISBN = "082189711X"
)
//@Citation(
//        value="Rodriguez, F. J. L. O. J., Oliver, J. L., Marín, A., & Medina, J. R. (1990). The general stochastic model of nucleotide substitution. Journal of theoretical biology, 142(4), 485-501.",
//        title = "The general stochastic model of nucleotide substitution",
//        authors = {"Rodriguez", "Oliver", "Marín", "Medina"},
//        year=1990,
//        DOI="https://doi.org/10.1016/S0022-5193(05)80104-3"
//)
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

    @GeneratorInfo(name = "gtr", verbClause = "is", narrativeName = "GTR model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"gtrCoalescent.lphy"},
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

    public Value<Double[]> getFreq() {
        return getParams().get(freqParamName);
    }
}
