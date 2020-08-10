package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Gamma distribution
 */
public class InverseGamma implements GenerativeDistribution1D<Double> {

    private final String shapeParamName;
    private final String scaleParamName;
    private Value<Double> alpha;
    private Value<Double> beta;

    GammaDistribution gammaDistribution;

    public InverseGamma(@ParameterInfo(name = "alpha", description = "the shape of the distribution.") Value<Double> alpha,
                        @ParameterInfo(name = "beta", description = "the scale of the distribution.") Value<Double> beta) {

        this.alpha = alpha;
        if (alpha == null) throw new IllegalArgumentException("The alpha value can't be null!");
        this.beta = beta;
        if (beta == null) throw new IllegalArgumentException("The beta value can't be null!");

        shapeParamName = getParamName(0);
        scaleParamName = getParamName(1);

        constructGammaDistribution();
    }

    @GeneratorInfo(name="InverseGamma", description = "The inverse-gamma probability distribution.")
    public RandomVariable<Double> sample() {
        constructGammaDistribution();
        double x = 1.0/gammaDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return gammaDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(shapeParamName, alpha);
        map.put(scaleParamName, beta);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(shapeParamName)) alpha = value;
        else if (paramName.equals(scaleParamName)) beta = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        constructGammaDistribution();
    }

    private void constructGammaDistribution() {
        // in case the shape is type integer
        double a = ((Number) alpha.value()).doubleValue();

        // in case the scale is type integer
        double b = ((Number) beta.value()).doubleValue();

        gammaDistribution = new GammaDistribution(a, b);
    }

    public String toString() {
        return getName();
    }

    public Value<Double> getBeta() {
        return beta;
    }

    public Value<Double> getAlpha() {
        return alpha;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};
    public Double[] getDomainBounds() {
        return domainBounds;
    }
}