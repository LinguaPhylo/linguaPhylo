package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

/**
 * Created by adru001 on 2/02/20.
 */
public abstract class RateMatrix extends DeterministicFunction<Double[][]> {

    public static final String meanRateParamName = "meanRate";

    public RateMatrix(@ParameterInfo(name = meanRateParamName, description = "the mean rate of the process. Default value is 1.0.", optional = true) Value<Number> rate) {
        if (rate != null) setParam(meanRateParamName, rate);
    }


    void normalize(Double[] freqs, Double[][] Q) {
        normalize(freqs, Q, totalRateDefault1());
    }

    void normalize(Double[] freqs, Double[][] Q, double rate) {
        // normalise rate matrix to one expected substitution per unit time
        double subst = 0.0;
        for (int i = 0; i < Q.length; i++) {
            subst += -Q[i][i] * freqs[i];
        }

        for (int i = 0; i < Q.length; i++) {
            for (int j = 0; j < Q.length; j++) {
                Q[i][j] = rate * (Q[i][j] / subst);
            }
        }
    }

    // Java Double matter
    Double[][] normalize(double[] freqs, double[][] Q, double rate) {
        Double[][] Qn = new Double[Q.length][Q.length];
        // normalise rate matrix to one expected substitution per unit time
        double subst = 0.0;
        for (int i = 0; i < Q.length; i++) {
            subst += -Q[i][i] * freqs[i];
        }

        for (int i = 0; i < Q.length; i++) {
            for (int j = 0; j < Q.length; j++) {
                Qn[i][j] = rate * (Q[i][j] / subst);
            }
        }
        return Qn;
    }

    public double totalRateDefault1() {
        Value<Double> meanRate = getMeanRate();
        if (meanRate != null) return meanRate.value();
        return 1.0;
    }

    public Value<Double> getMeanRate() {
        return getParams().get(meanRateParamName);
    }
}
