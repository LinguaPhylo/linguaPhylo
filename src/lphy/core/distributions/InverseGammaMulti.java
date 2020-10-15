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

    private Value<Number> shape;
    private Value<Number> scale;
    private Value<Integer> n;

    GammaDistribution gammaDistribution;

    public InverseGammaMulti(@ParameterInfo(name = shapeParamName, description = "the shape of the distribution.") Value<Number> shape,
                             @ParameterInfo(name = scaleParamName, description = "the scale of the distribution.") Value<Number> scale,
                             @ParameterInfo(name = nParamName, description = "the dimension of the return.") Value<Integer> n) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The " + shapeParamName + " value can't be null!");
        this.scale = scale;
        if (scale == null) throw new IllegalArgumentException("The " + scaleParamName + " value can't be null!");
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
            put(shapeParamName, shape);
            put(scaleParamName, scale);
            put(nParamName, n);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case shapeParamName:
                shape = value;
                break;
            case scaleParamName:
                scale = value;
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
        double a = doubleValue(shape);

        // in case the scale is type integer
        double b = doubleValue(scale);

        gammaDistribution = new GammaDistribution(a, b);
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