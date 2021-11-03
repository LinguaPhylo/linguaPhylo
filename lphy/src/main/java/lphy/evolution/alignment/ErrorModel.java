package lphy.evolution.alignment;

import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class ErrorModel implements GenerativeDistribution<Alignment> {

    Value<Double> alpha;
    Value<Double> beta;
    Value<Alignment> alignment;

    public final String alphaParamName = "alpha";
    public final String betaParamName = "beta";
    public final String alignmentParamName = "alignment";

    RandomGenerator random;

    public ErrorModel(@ParameterInfo(name = alphaParamName, description = "the false positive probability.") Value<Double> alpha,
                      @ParameterInfo(name = betaParamName, description = "the false negative probability.") Value<Double> beta,
                      @ParameterInfo(name = alignmentParamName, description = "the alignment without errors.") Value<Alignment> alignment) {

        this.alpha = alpha;
        this.beta = beta;
        this.alignment = alignment;
        this.random = Utils.getRandom();
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(alphaParamName, alpha);
        map.put(betaParamName, beta);
        map.put(alignmentParamName, alignment);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(alphaParamName)) {
            if (value.value() instanceof Double) {
                alpha = value;
            } else throw new IllegalArgumentException("Expecting type double, but got " + value.value().getClass());
        }
        else if (paramName.equals(betaParamName)) beta = value;
        else if (paramName.equals(alignmentParamName)) alignment = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    @GeneratorInfo(name = "ErrorModel", description = "The error model distribution on an alignment.")
    public RandomVariable<Alignment> sample() {

        Alignment original = alignment.value();
        SimpleAlignment newAlignment = new ErrorAlignment(original.nchar(), original);

        double a = alpha.value();
        double b = beta.value();

        for (int i = 0; i < newAlignment.ntaxa(); i++) {
            for (int j = 0; j < newAlignment.nchar(); j++) {
                newAlignment.setState(i, j, error(original.getState(i, j), a, b));
            }
        }

        return new RandomVariable<>("D", newAlignment, this);
    }

    private int error(int state, double alpha, double beta) {

        double U = random.nextDouble();
        switch (state) {
            case 0:
                if (U < alpha) return 1;
                return 0;
            case 1: default:
                if (U < beta) return 0;
                return 1;
        }
    }
}
