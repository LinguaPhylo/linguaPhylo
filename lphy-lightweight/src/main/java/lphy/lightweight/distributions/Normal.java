package lphy.base.lightweight.distributions;

import lphy.base.lightweight.LGenerativeDistribution;
import lphy.core.model.component.GeneratorInfo;
import lphy.core.model.component.ParameterInfo;
import lphy.core.util.RandomUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Normal distribution
 */
public class Normal implements LGenerativeDistribution<Double> {

    private Double mean;
    private Double sd;

    private RandomGenerator random;

    NormalDistribution normalDistribution;

    public Normal(@ParameterInfo(name = "mean", description = "the mean of the distribution.") Double mean,
                  @ParameterInfo(name = "sd", description = "the standard deviation of the distribution.") Double sd) {

        this.mean = mean;
        if (mean == null) throw new IllegalArgumentException("The mean value can't be null!");
        this.sd = sd;
        if (sd == null) throw new IllegalArgumentException("The sd value can't be null!");
        random = RandomUtils.getRandom();
    }

    @GeneratorInfo(name="Normal", description="The normal probability distribution.")
    public Double sample() {

        // in case the mean is type integer
        double d =((Number)mean).doubleValue();

        normalDistribution = new NormalDistribution(d, sd);
        return normalDistribution.sample();
    }

    @Override
    public double density(Double x) {
        return normalDistribution.density(x);
    }

    public Double getMean() {
        return mean;
    }

    public Double getSd() {
        return sd;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public void setSd(Double sd) {
        this.sd = sd;
    }

    public String toString() {
        return getName();
    }

}