package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.*;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Gamma distribution
 */
public class InverseGammaMulti implements GenerativeDistribution<Double[]> {

    private Value<Number> alpha;
    private Value<Number> beta;
    private Value<Integer> n;

    GammaDistribution gammaDistribution;

    public InverseGammaMulti(@ParameterInfo(name = alphaParamName, description = "the shape of the distribution.") Value<Number> alpha,
                             @ParameterInfo(name = betaParamName, description = "the scale of the distribution.") Value<Number> beta,
                             @ParameterInfo(name = nParamName, description = "the dimension of the return.") Value<Integer> n) {

        this.alpha = alpha;
        if (alpha == null) throw new IllegalArgumentException("The " + alphaParamName + " value can't be null!");
        this.beta = beta;
        if (beta == null) throw new IllegalArgumentException("The " + betaParamName + " value can't be null!");
        this.n = n;

        constructGammaDistribution();
    }

    @GeneratorInfo(name = "InverseGamma", description = "The inverse-gamma probability distribution.")
    public RandomVariable<Double[]> sample() {
        constructGammaDistribution();
        Double[] x = new Double[n.value()];
        for (int i =0; i < x.length; i++) {
            x[i] = 1.0 / gammaDistribution.sample();
        }
        return new RandomVariable<>(null, x, this);
    }

    public double density(Double x) {
        throw new UnsupportedOperationException();
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(alphaParamName, alpha);
            put(betaParamName, beta);
            put(nParamName, n);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case alphaParamName:
                alpha = value;
                break;
            case betaParamName:
                beta = value;
                break;
            case nParamName:
                n = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }

        constructGammaDistribution();
    }

    private void constructGammaDistribution() {
        // in case the shape is type integer
        double a = doubleValue(alpha);

        // in case the scale is type integer
        double b = doubleValue(beta);

        gammaDistribution = new GammaDistribution(a, 1.0/b);
    }

    public String toString() {
        return getName();
    }

    public Value<Number> getBeta() {
        return beta;
    }

    public Value<Number> getAlpha() {
        return alpha;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}