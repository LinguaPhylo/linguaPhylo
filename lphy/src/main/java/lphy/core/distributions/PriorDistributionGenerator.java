package lphy.core.distributions;

import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.Value;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Discrete distributions (Integer) and continuous distributions (Double)
 */
public abstract class PriorDistributionGenerator<T> implements GenerativeDistribution<T> {

    protected RandomGenerator random;

    /**
     * This gets the cached pseudo-random number generator
     */
    public PriorDistributionGenerator() {
        random = Utils.getRandom();
        // call constructDistribution() after setting class field parameters
    }

    /**
     * Create the instance of distribution class given the parameter(s),
     * and cache it in order to reuse in sample() and density().
     * It should be only called in constructor and setParam(),
     * or any setters to change parameter value.
     */
    protected abstract void constructDistribution(RandomGenerator random);


    @Override
    public void setParam(String paramName, Value<?> value) {
        GenerativeDistribution.super.setParam(paramName, value);

        constructDistribution(random);
    }

    public String toString() {
        return getName();
    }
}
