package lphy.base.evolution.substitutionmodel;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.ParameterInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
public abstract class RateMatrix extends DeterministicFunction<Double[][]> {

    public static final String meanRateParamName = "meanRate";

    public RateMatrix(@ParameterInfo(name = meanRateParamName, description = "the mean rate of the process. Default value is 1.0.", optional = true) Value<Number> rate) {
        if (rate != null) setParam(meanRateParamName, rate);
    }

    // regular eigen decomposition methods used by default
    public boolean canReturnComplexDiagonalization() { return false; }

    /**
     * If this is overwritten, the child class needs to call super.{@link #normalize(Double[], Double[][])},
     * to make sure the mean rate is involved.
     * @param freqs
     * @param Q
     */
    void normalize(final Double[] freqs, Double[][] Q) {
        normalize(freqs, Q, totalRateDefault1());
    }

    protected void normalize(final Double[] freqs, Double[][] Q, double rate) {
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
    Double[][] normalize(final double[] freqs, double[][] Q, double rate) {
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

    public static void main(String... args) throws ClassNotFoundException {

        Constructor constructor = RateMatrix.class.getConstructors()[0];

        System.out.println("Generic parameter types");
        Type[] generics = constructor.getGenericParameterTypes();
        for (int i = 0; i < generics.length; i++) {
            String name = generics[i].getTypeName();

            System.out.println("name=" + name);
            System.out.println("class=" + Class.forName(name.substring(name.indexOf('<')+1, name.indexOf('>'))));
        }

        System.out.println("Type variables");
        TypeVariable[] typeVariables = constructor.getTypeParameters();
        for (int i = 0; i < typeVariables.length; i++) {
            System.out.println("name=" + typeVariables[i].getName());
        }

    }
}
