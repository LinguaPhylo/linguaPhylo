package lphy.base.lightweight.distributions;

import lphy.base.lightweight.LGenerativeDistribution;
import lphy.core.model.components.GeneratorInfo;
import lphy.core.model.components.ParameterInfo;
import lphy.core.util.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Uniform implements LGenerativeDistribution<Double> {

    private Double lower;
    private Double upper;

    private RandomGenerator random;

    public Uniform(@ParameterInfo(name = "lower", description = "the lower bound of the uniform distribution.") Double lower,
                   @ParameterInfo(name = "upper", description = "the upper bound of the uniform distribution.") Double upper) {

        this.lower = lower;
        this.upper = upper;
        this.random = RandomUtils.getRandom();
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