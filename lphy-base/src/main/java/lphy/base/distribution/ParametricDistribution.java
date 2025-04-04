package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution;
import lphy.core.model.Generator;
import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
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
     * to set parameters automatically if there are setter,
     * and call {@link #constructDistribution(RandomGenerator)}.
     * Note: if overwrite this, it must have <code>super.setParam(paramName, value);</code> in the end.
     *
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
