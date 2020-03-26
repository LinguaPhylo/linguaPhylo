package lphy.core.lightweight.distributions;

import lphy.core.distributions.Utils;
import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by adru001 on 18/12/19.
 */
public class Uniform implements LightweightGenerativeDistribution<Double> {

    private Double lower;
    private Double upper;

    private RandomGenerator random;

    public Uniform(@ParameterInfo(name = "lower", description = "the lower bound of the uniform distribution.") Double lower,
                   @ParameterInfo(name = "upper", description = "the upper bound of the uniform distribution.") Double upper) {

        this.lower = lower;
        this.upper = upper;
        this.random = Utils.getRandom();
    }

    @GeneratorInfo(name="Uniform", description="The uniform probability distribution.")
    public Double sample() {
        return random.nextDouble() * (upper-lower) + lower;
    }

    public double density(Double x) {
        return 1.0 / (upper-lower);
    }

    public Double getLower() {
        return lower;
    }

    public Double getUpper() {
        return upper;
    }

    public void setLower(Double lower) {
        this.lower = lower;
    }

    public void setUpper(Double upper) {
        this.upper = upper;
    }

    public String toString() {
        return getName();
    }
}