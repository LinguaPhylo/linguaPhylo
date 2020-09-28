package lphy.core.lightweight.distributions;

import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

/**
 * Gamma distribution
 */
public class Gamma implements LightweightGenerativeDistribution<Double> {

    private Double shape;
    private Double scale;

    GammaDistribution gammaDistribution;

    public Gamma(@ParameterInfo(name = "shape", description = "the shape of the distribution.") Double shape,
                 @ParameterInfo(name = "scale", description = "the scale of the distribution.") Double scale) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The shape value can't be null!");
        this.scale = scale;
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");

        constructGammaDistribution();
    }

    @GeneratorInfo(name = "Gamma", description = "The gamma probability distribution.")
    public Double sample() {
        constructGammaDistribution();
        return gammaDistribution.sample();
    }

    public Double getShape() {
        return shape;
    }

    public Double getScale() {
        return scale;
    }

    public void setShape(Double shape) {
        this.shape = shape;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    @Override
    public double density(Double x) {
        return gammaDistribution.density(x);
    }

    private void constructGammaDistribution() {
        // in case the shape is type integer
        double sh = ((Number) shape).doubleValue();

        // in case the scale is type integer
        double sc = ((Number) scale).doubleValue();

        gammaDistribution = new GammaDistribution(sh, sc);
    }

    public String toString() {
        return getName();
    }
}