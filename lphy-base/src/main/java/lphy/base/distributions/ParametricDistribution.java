package lphy.base.distributions;

import lphy.core.graphicalmodel.components.GenerativeDistribution;
import lphy.core.graphicalmodel.components.Generator;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.util.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * The template for discrete distributions (Integer) and continuous distributions (Double).
 * Call super() to get random number generator,
 * and constructDistribution(RandomGenerator) in the last line of constructor.
 * This setParam handles all parameters automatically,
 * so that the child class of prior distribution does not require to implement setParam() method.
 * The example classes are Beta, Gamma, ...
 * @author Walter Xie
 * @author Alexei Drummond
 */
public abstract class ParametricDistribution<T> implements GenerativeDistribution<T> {

    protected RandomGenerator random;

    /**
     * This gets the cached pseudo-random number generator
     */
    public ParametricDistribution() {
        random = RandomUtils.getRandom();
        // call constructDistribution(RandomGenerator) after setting class field parameters
    }

    /**
     * Create the instance of distribution class given the parameter(s),
     * and cache it in order to reuse in sample() and density().
     * It should be only called in the last line of constructor (required to call super()),
     * and any additional setters to change parameter value.
     */
    protected abstract void constructDistribution(RandomGenerator random);

    /**
     * Call {@link Generator#setParam(String, Value)}
     * to set all parameters automatically, and call {@link #constructDistribution(RandomGenerator)}.
     * So the implemented prior does not require to implement this method.
     * @param paramName   parameter name
     * @param value       {@link Value} contains parameter value.
     */
    @Override
    public void setParam(String paramName, Value<?> value) {
        GenerativeDistribution.super.setParam(paramName, value);

        constructDistribution(random);
    }

    public String toString() {
        return getName();
    }
}
