package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.DeterministicFunction;

/**
 * Created by adru001 on 2/02/20.
 */
public abstract class RateMatrix extends DeterministicFunction<Double[][]> {

    void normalize(Double[] freqs, Double[][] Q) {
        // normalise rate matrix to one expected substitution per unit time
        double subst = 0.0;
        for (int i = 0; i < Q.length; i++) {
            subst += -Q[i][i] * freqs[i];
        }

        for (int i = 0; i < Q.length; i++) {
            for (int j = 0; j < Q.length; j++) {
                Q[i][j] = Q[i][j] / subst;
            }
        }
    }

    // Java Double matter
    Double[][] normalize(double[] freqs, double[][] Q) {
        Double[][] Qn = new Double[Q.length][Q.length];
        // normalise rate matrix to one expected substitution per unit time
        double subst = 0.0;
        for (int i = 0; i < Q.length; i++) {
            subst += -Q[i][i] * freqs[i];
        }

        for (int i = 0; i < Q.length; i++) {
            for (int j = 0; j < Q.length; j++) {
                Qn[i][j] = Q[i][j] / subst;
            }
        }
        return Qn;
    }
}
