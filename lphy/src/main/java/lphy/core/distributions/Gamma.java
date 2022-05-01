package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.scaleParamName;
import static lphy.core.distributions.DistributionConstants.shapeParamName;

/**
 * Gamma distribution
 */
public class Gamma implements GenerativeDistribution1D<Double> {

    private Value<Double> shape;
    private Value<Double> scale;

    GammaDistribution gammaDistribution;

    public Gamma(@ParameterInfo(name = shapeParamName, description = "the shape of the distribution.") Value<Double> shape,
                 @ParameterInfo(name = scaleParamName, description = "the scale of the distribution.") Value<Double> scale) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        this.scale = scale;
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");

        constructGammaDistribution();
    }

    @GeneratorInfo(name = "Gamma", verbClause = "has", narrativeName = "gamma distribution prior",
            category = GeneratorCategory.PROB_DIST, examples = {"covidDPG.lphy"},
            description = "The gamma probability distribution.")
    public RandomVariable<Double> sample() {
        constructGammaDistribution();
        double x = gammaDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return gammaDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(shapeParamName, shape);
            put(scaleParamName, scale);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(shapeParamName)) shape = value;
        else if (paramName.equals(scaleParamName)) scale = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        constructGammaDistribution();
    }

    private void constructGammaDistribution() {
        // in case the shape is type integer
        double sh = ((Number) shape.value()).doubleValue();

        // in case the scale is type integer
        double sc = ((Number) scale.value()).doubleValue();

        gammaDistribution = new GammaDistribution(Utils.getRandom(), sh, sc);
    }

    public String toString() {
        return getName();
    }

    public Value<Double> getScale() {
        return scale;
    }

    public Value<Double> getShape() {
        return shape;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}