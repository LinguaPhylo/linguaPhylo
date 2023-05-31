package lphy.base.evolution.substitutionmodel;

import lphy.core.graphicalmodel.components.*;
import lphy.core.graphicalmodel.types.DoubleArray2DValue;

/**
 * K80: AC = AT = CG = GT, AG = CT, equal base frequencies, k + 1 free parameters
 * @author Alexei Drummond
 */
@Citation(value="Kimura, M. A simple method for estimating evolutionary rates of base substitutions\n" +
        "through comparative studies of nucleotide sequences. J Mol Evol 16, 111–120 (1980). ",
        title = "A simple method for estimating evolutionary rates of base substitutions\n" +
                "through comparative studies of nucleotide sequences",
        year = 1980,
        authors = {"Kimura"},
        DOI="https://doi.org/10.1007/BF01731581")
public class K80 extends RateMatrix {

    public static final String kappaParamName = "kappa";

    public K80(@ParameterInfo(name = kappaParamName, description = "the kappa of the K80 process.") Value<Double> kappa,
               @ParameterInfo(name = meanRateParamName, description = "the mean rate of the K80 process. default 1.0", optional = true) Value<Number> meanRate) {

        super(meanRate);
        setParam(kappaParamName, kappa);
    }


    @GeneratorInfo(name = "k80", verbClause = "is", narrativeName = "K80 model",
            category = GeneratorCategory.RATE_MATRIX,
            description = "The K80 instantaneous rate matrix. Takes a kappa and produces a K80 rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double> kappa = getKappa();
        return new DoubleArray2DValue(k80(kappa.value()), this);
    }

    private Double[][] k80(double kappa) {

        int numStates = 4;

        Double[][] Q = new Double[numStates][numStates];

        double[] totalRates = new double[numStates];

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                if (i != j) {
                    if (Math.abs(i-j) == 2) {
                        Q[i][j] = kappa;
                    } else {
                        Q[i][j] = 1.0;
                    }
                }
                totalRates[i] += Q[i][j];
            }
            Q[i][i] = -totalRates[i];
        }

        normalize(new Double[] {0.25, 0.25, 0.25, 0.25}, Q, totalRateDefault1());

        return Q;
    }

    public Value<Double> getKappa() {
        return getParams().get(kappaParamName);
    }

}
