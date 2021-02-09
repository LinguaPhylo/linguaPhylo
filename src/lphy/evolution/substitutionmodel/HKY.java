package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

import java.util.Map;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by adru001 on 2/02/20.
 */
@Citation(
        value="Hasegawa, M., Kishino, H. & Yano, T. Dating of the human-ape splitting by a molecular clock of mitochondrial DNA. J Mol Evol 22, 160–174 (1985)",
        year = 1985,
        authors = {"Hasegawa", "Kishino", "Yano"},
        DOI="https://doi.org/10.1007/BF02101694")
public class HKY extends RateMatrix {

    public static final String kappaParamName = "kappa";
    public static final String freqParamName =  "freq";


    public HKY(@ParameterInfo(name = kappaParamName, narrativeName = "transition bias parameter", description = "the kappa of the HKY process.") Value<Number> kappa,
               @ParameterInfo(name = freqParamName, narrativeName="base frequencies", description = "the base frequencies.") Value<Double[]> freq,
               @ParameterInfo(name = meanRateParamName, description = "the total rate of substitution per unit time. Default 1.0.", optional = true) Value<Number> rate) {

        super(rate);

        setParam(kappaParamName, kappa);
        setParam(freqParamName, freq);
    }


    @GeneratorInfo(name = "hky",
            verbClause = "is",
            narrativeName = "HKY model",
            description = "The HKY instantaneous rate matrix. Takes a kappa and base frequencies (and optionally a total rate) and produces an HKY85 rate matrix.")
    public Value<Double[][]> apply() {

        Map<String, Value> params = getParams();
        double kappa = doubleValue((Value<Number>)params.get(kappaParamName));
        Double[] freq = ((Value<Double[]>)params.get(freqParamName)).value();

        return new DoubleArray2DValue(hky(kappa, freq), this);
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

        // normalise rate matrix to rate
        normalize(freqs, Q);

        return Q;
    }

    public static void main(String... args) throws ClassNotFoundException {

        System.out.println(Double[].class.getName());

        System.out.println(Class.forName("[Ljava.lang.Double;"));
    }
}
