package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.DoubleArray2DValue;

/**
 * Description is copied from https://taming-the-beast.org/tutorials/LanguagePhylogenies/ :
 *
 * In the Binary Covarion model each character can have one of four states
 * that are divided into visible states and hidden states.
 * The visible ones are 0 and 1 and the hidden ones are fast and slow.
 * Characters change their binary state at a rate 1 in the fast state and at rate alpha in the slow state.
 * Furthermore, they are allowed to change from one of the hidden categories to another at a switch rate s.
 *
 * This implements the BEAST mode in BEAST 2.
 */
public class BinaryCovarion extends RateMatrix {

    public static final String AlphaParamName = "alpha";
    public static final String SwitchRateParamName = "s";
    public static final String vfreqParamName = "vfreq";
    public static final String hfreqParamName = "hfreq";

    private final int NumOfStates = 4;

    public BinaryCovarion(@ParameterInfo(name = AlphaParamName, description = "the rate of evolution in slow mode.") Value<Number> alpha,
                          @ParameterInfo(name = SwitchRateParamName, description = "the rate of flipping between slow and fast modes") Value<Number> s,
                          @ParameterInfo(name = vfreqParamName, description = "the frequencies of the visible states") Value<Number[]> vfreq,
                          @ParameterInfo(name = hfreqParamName, description = "the frequencies of the hidden rates") Value<Number[]> hfreq,
                          @ParameterInfo(name = meanRateParamName, description = "the mean rate of the process. default = 1.0", optional = true) Value<Number> meanRate) {

        super(meanRate);

        setParam(AlphaParamName, alpha);
        setParam(SwitchRateParamName, s);
        setParam(vfreqParamName, vfreq);
        setParam(hfreqParamName, hfreq);
    }

    @GeneratorInfo(name = "binaryCovarion", verbClause = "is", narrativeName = "Binary Covarion model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"cpacific.lphy"},
            description = "The rate matrix of the Covarion model for Binary data. It is equivalent to the BEAST mode.")
    public Value<Double[][]> apply() {

        double alpha = ValueUtils.doubleValue(getParams().get(AlphaParamName));
        double switchRate = ValueUtils.doubleValue(getParams().get(SwitchRateParamName));

        double[] vfreq = ValueUtils.doubleArrayValue(getParams().get(vfreqParamName));
        double[] hfreq = ValueUtils.doubleArrayValue(getParams().get(hfreqParamName));

        Double[][] Q = setupUnnormalizedQMatrix(alpha, switchRate, vfreq, hfreq);
        Double[] freqs = getFrequencies(vfreq, hfreq);

        // normalise rate matrix to one expected substitution per unit time
        normalize(freqs, Q);

        return new DoubleArray2DValue(Q, this);
    }

    private Double[][] setupUnnormalizedQMatrix(double a, double s, double[] vf, double[] hf) {
        double f0 = hf[0];
        double f1 = hf[1];
        double p0 = vf[0];
        double p1 = vf[1];

        assert Math.abs(1.0 - f0 - f1) < 1e-8;
        assert Math.abs(1.0 - p0 - p1) < 1e-8;

        Double[][] unnormalizedQ = new Double[NumOfStates][NumOfStates];

        unnormalizedQ[0][1] = a * p1;
        unnormalizedQ[0][2] = s;
        unnormalizedQ[0][3] = 0.0;

        unnormalizedQ[1][0] = a * p0;
        unnormalizedQ[1][2] = 0.0;
        unnormalizedQ[1][3] = s;

        unnormalizedQ[2][0] = s;
        unnormalizedQ[2][1] = 0.0;
        unnormalizedQ[2][3] = p1;

        unnormalizedQ[3][0] = 0.0;
        unnormalizedQ[3][1] = s;
        unnormalizedQ[3][2] = p0;

        // set up diagonal
        for (int i = 0; i < NumOfStates; i++) {
            double sum = 0.0;
            for (int j = 0; j < NumOfStates; j++) {
                if (i != j)
                    sum += unnormalizedQ[i][j];
            }
            unnormalizedQ[i][i] = -sum;
        }

        return unnormalizedQ;
    }

    void normalize(Double[] freqs, Double[][] Q) {
//        double subst = 0.0;
//        int dimension = freqs.length;
//
//        for (int i = 0; i < dimension; i++) {
//            subst += -Q[i][i] * freqs[i];
//        }
//
//        // normalize, including switches
//        for (int i = 0; i < dimension; i++) {
//            for (int j = 0; j < dimension; j++) {
//                Q[i][j] = Q[i][j] / subst;
//            }
//        }
        // this take mean rate
        super.normalize(freqs, Q);

        double switchingProportion = 0.0;
        switchingProportion += Q[0][2] * freqs[2];
        switchingProportion += Q[2][0] * freqs[0];
        switchingProportion += Q[1][3] * freqs[3];
        switchingProportion += Q[3][1] * freqs[1];

        //System.out.println("switchingProportion=" + switchingProportion);

        // normalize, removing switches
        for (int i = 0; i < Q.length; i++) {
            for (int j = 0; j < Q.length; j++) {
                Q[i][j] = Q[i][j] / (1.0 - switchingProportion);
            }
        }
    }

    public Double[] getFrequencies(double[] vf, double[] hf) {
        Double[] freqs = new Double[NumOfStates];
        freqs[0] = vf[0] * hf[0];
        freqs[1] = vf[1] * hf[0];
        freqs[2] = vf[0] * hf[1];
        freqs[3] = vf[1] * hf[1];
        return freqs;
    }


}
