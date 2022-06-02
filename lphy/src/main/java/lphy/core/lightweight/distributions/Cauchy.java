package lphy.core.lightweight.distributions;

import lphy.core.lightweight.LGenerativeDistribution;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.util.RandomUtils;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Cauchy distribution
 */
public class Cauchy implements LGenerativeDistribution<Double> {

    private Double mean;
    private Double scale;

    private RandomGenerator random;

    CauchyDistribution cauchyDistribution;

    public Cauchy(@ParameterInfo(name = "mean", description = "the mean of the Cauchy distribution.") Double mean,
                  @ParameterInfo(name = "scale", description = "the scale of the Cauchy distribution.") Double scale) {

        this.mean = mean;
        if (mean == null) throw new IllegalArgumentException("The mean value can't be null!");
        this.scale = scale;
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");
        random = RandomUtils.getRandom();

        constructCauchyDistribution();
    }

    @GeneratorInfo(name="Cauchy", description = "The Cauchy probability distribution.")
    public Double sample() {
        constructCauchyDistribution();
        return cauchyDistribution.sample();
    }

    public double density(Double x) {
        return cauchyDistribution.density(x);
    }

    public Double getMean() {
        return mean;
    }

    public Double getScale() {
        return scale;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    private void constructCauchyDistribution() {
        // in case the shape is type integer
        double mean = ((Number) this.mean).doubleValue();

        // in case the scale is type integer
        double sc = ((Number) scale).doubleValue();

        cauchyDistribution = new CauchyDistribution(mean, sc);
    }

    public String toString() {
        return getName();
    }

}