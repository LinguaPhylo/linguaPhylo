package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.offsetParamName;

/**
 * The discrete probability distribution of
 * the number of events when the expected number of events is lambda.
 * @see PoissonDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Poisson extends ParametricDistribution<Integer> implements GenerativeDistribution1D<Integer> {

    private static final String lambdaParamName = "lambda";
    private static final String minParamName = "min";
    private static final String maxParamName = "max";
    private Value<Number> lambda;
    private Value<Integer> min;
    private Value<Integer> max;
    private Value<Integer> offset;

    PoissonDistribution poisson;

    static final int MAX_TRIES = 10000;

    public Poisson(@ParameterInfo(name=lambdaParamName, description="the expected number of events.") Value<Number> lambda,
                   @ParameterInfo(name= offsetParamName, optional = true,
                           description = "optional parameter to add a constant to the returned result, default is 0") Value<Integer> offset,
                   @ParameterInfo(name=minParamName, optional = true,
                           description = "optional parameter to specify a condition that the number of events " +
                                   "must be greater than or equal to this mininum, default is 0.") Value<Integer> min,
                   @ParameterInfo(name=maxParamName, optional = true,
                           description = "optional parameter to specify a condition that the number of events " +
                                   "must be less than or equal to this maximum") Value<Integer> max) {
        super();
        this.lambda = lambda;
        this.min = min;
        this.max = max;
        this.offset = offset;

        constructDistribution(random);
    }

    public Poisson(@ParameterInfo(name=lambdaParamName, description="the expected number of events.")
                   Value<Number> lambda) {
        super();
        this.lambda = lambda;
        this.min = null;
        this.max = null;
        this.offset = null;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        poisson = new PoissonDistribution(RandomUtils.getRandom(), ValueUtils.doubleValue(lambda),
                PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
    }

    @GeneratorInfo(name="Poisson",
            category = GeneratorCategory.PRIOR,
            examples = {"expression4.lphy","simpleRandomLocalClock2.lphy"},
            description="The probability distribution of the number of events when the expected number of events is lambda, supported on the set { 0, 1, 2, 3, ... }.")
    public RandomVariable<Integer> sample() {

        // constructDistribution() only required in constructor and setParam

        int minimum = min();
        int maximum = max();

        int val = -1;
        int count = 0;
        while (val < minimum || val > maximum) {
            val = poisson.sample() + C();
            count += 1;
            if (count > MAX_TRIES) {
                throw new RuntimeException("Failed to draw conditional Poisson random variable after " + MAX_TRIES + " attempts.");
            }
        }

        return new RandomVariable<>(null, val, this);
    }

    private int C() {
        int C = 0;
        if (offset != null) {
            C = offset.value();
        }
        return C;
    }

    private int min() {
        if (min != null) return min.value();
        return 0;
    }

    private int max() {
        if (max != null) return max.value();
        return Integer.MAX_VALUE;
    }

    public double density(Integer i) {
        if (i < min()) return 0.0;
        if (i > max()) return 0.0;
        return poisson.probability(i-C());
    }

    @Override
    public Map<String,Value> getParams() {
        return new TreeMap<>() {{
            put(lambdaParamName, lambda);
            if (min != null) put(minParamName, min);
            if (max != null) put(maxParamName, max);
            if (offset != null) put(offsetParamName, offset);
        }};    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(lambdaParamName)) lambda = value;
        else if (paramName.equals(minParamName)) min = value;
        else if (paramName.equals(maxParamName)) max = value;
        else if (paramName.equals(offsetParamName)) offset = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }

//    public void setLambda(double p) {
//        this.lambda.setValue(p);
//        constructDistribution(random);
//    }

    private static final Integer[] domainBounds = {0, Integer.MAX_VALUE};
    public Integer[] getDomainBounds() {
        return domainBounds;
    }

    public Value<Number> getLambda() {
        return lambda;
    }

    public Value<Integer> getMin() {
        return min;
    }

    public Value<Integer> getMax() {
        return max;
    }

    public Value<Integer> getOffset() {
        return offset;
    }

}
