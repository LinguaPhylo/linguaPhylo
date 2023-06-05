package lphy.base.evolution.substitutionmodel;

import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.model.component.Value;
import lphy.core.model.datatype.DoubleArray2DValue;

/**
 * TN93: AC = AT = CG = GT, AG, CT, unequal base frequencies, k + 5 free parameters
 * @author Alexei Drummond
 */
@Citation(value="Tamura K,  Nei M. Estimation of the number of nucleotide substitutions " +
        "in the control region of mitochondrial DNA in humans and chimpanzees, " +
        "Mol Biol Evol, 1993, vol. 10 (pg. 512-526)",
        title = "Estimation of the number of nucleotide substitutions " +
                "in the control region of mitochondrial DNA in humans and chimpanzees",
        year = 1993,
        authors = {"Tamura", "Nei"},
        DOI="https://doi.org/10.1093/oxfordjournals.molbev.a040023")
public class TN93 extends RateMatrix {

    public static final String kappa1ParamName = "kappa1";
    public static final String kappa2ParamName = "kappa2";
    public static final String freqParamName = "freq";

    public TN93(@ParameterInfo(name = kappa1ParamName, description = "the rate of A<->G transition in the TN93 process.") Value<Double> kappa1,
                @ParameterInfo(name = kappa2ParamName, description = "the rate of C<->T transition in the TN93 process.") Value<Double> kappa2,
                @ParameterInfo(name = freqParamName, description = "the base frequencies.") Value<Double[]> freq,
                @ParameterInfo(name = RateMatrix.meanRateParamName, description = "the mean rate of the process. default 1.0", optional = true) Value<Number> meanRate) {

        super(meanRate);
        setParam(kappa1ParamName, kappa1);
        setParam(kappa2ParamName, kappa2);
        setParam(freqParamName, freq);
    }


    @GeneratorInfo(name = "tn93", verbClause = "is", narrativeName = "HKY model",
            category = GeneratorCategory.RATE_MATRIX,
            description = "The TN93 instantaneous rate matrix. Takes kappa1, kappa2 and base frequencies and produces an Tamura-Nei-93 rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double> kappa1 = getKappa1();
        Value<Double> kappa2 = getKappa2();
        Value<Double[]> freq = getParams().get(freqParamName);
        return new DoubleArray2DValue(getName() + "(" + kappa1.getLabel() + ", " + kappa2.getLabel() + ", " + freq.getLabel() + ")", hky(kappa1.value(), kappa2.value(), freq.value()), this);
    }

    private Double[][] hky(double kappa1, double kappa2, Double[] freqs) {

        int numStates = 4;

        Double[][] Q = {
                {0.0, freqs[1], freqs[2] * kappa1, freqs[3]},
                {freqs[0], 0.0, freqs[2], freqs[3] * kappa2},
                {freqs[0] * kappa1, freqs[1], 0.0, freqs[3]},
                {freqs[0], freqs[1] * kappa2, freqs[2], 0.0}
        };

        double[] totalRates = new double[numStates];


        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                totalRates[i] += Q[i][j];
            }
        }

        normalize(freqs, Q);

        return Q;
    }

    public Value<Double> getKappa1() {
        return getParams(). get(kappa1ParamName);
    }

    public Value<Double> getKappa2() {
        return getParams(). get(kappa2ParamName);
    }

    public Value<Double[]> getFreq() {
        return getParams().get(freqParamName);
    }

}
