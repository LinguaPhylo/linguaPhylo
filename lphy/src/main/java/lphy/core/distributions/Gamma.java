package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.scaleParamName;
import static lphy.core.distributions.DistributionConstants.shapeParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Gamma distribution
 */
public class Gamma implements GenerativeDistribution1D<Double> {

    private Value<Number> shape;
    private Value<Number> scale;

    GammaDistribution gammaDistribution;

    public Gamma(@ParameterInfo(name = shapeParamName, description = "the shape of the distribution.") Value<Number> shape,
                 @ParameterInfo(name = scaleParamName, description = "the scale of the distribution.") Value<Number> scale) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        this.scale = scale;
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");

        constructDistribution();
    }

    @GeneratorInfo(name = "Gamma", verbClause = "has", narrativeName = "gamma distribution prior",
            category = GeneratorCategory.PROB_DIST, examples = {"covidDPG.lphy"},
            description = "The gamma probability distribution.")
    public RandomVariable<Double> sample() {
        // constructDistribution() only required in constructor and setParam
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

        constructDistribution();
    }

    @Override
    public void constructDistribution() {
        // in case the shape is type integer
        double sh = doubleValue(shape);

        // in case the scale is type integer
        double sc = doubleValue(scale);

        gammaDistribution = new GammaDistribution(Utils.getRandom(), sh, sc);
    }

    public String toString() {
        return getName();
    }

    public Value<Number> getScale() {
        return scale;
    }

    public Value<Number> getShape() {
        return shape;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}