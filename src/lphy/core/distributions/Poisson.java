package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by adru001 on 18/12/19.
 */
public class Poisson implements GenerativeDistribution1D<Integer> {

    private static final String lambdaParamName = "lambda";
    private static final String minParamName = "min";
    private static final String maxParamName = "max";
    private Value<Number> lambda;
    private Value<Integer> min;
    private Value<Integer> max;

    static final int MAX_TRIES = 10000;

    public Poisson(@ParameterInfo(name=lambdaParamName, description="the expected number of events.") Value<Number> lambda,
                   @ParameterInfo(name=minParamName, optional = true, description = "optional parameter to specify a condition that the number of events must be greater than or equal to this mininum") Value<Integer> min,
                   @ParameterInfo(name=maxParamName, optional = true, description = "optional parameter to specify a condition that the number of events must be less than or equal to this maximum") Value<Integer> max)
            {
        this.lambda = lambda;
        this.min = min;
        this.max = max;
    }

    @GeneratorInfo(name="Poisson", description="The probability distribution of the number of events when the expected number of events is lambda, supported on the set { 0, 1, 2, 3, ... }.")
    public RandomVariable<Integer> sample() {

        PoissonDistribution poisson = new PoissonDistribution(doubleValue(lambda));

        int minimum = 0;
        int maximum = Integer.MAX_VALUE;
        if (min != null) minimum = min.value();
        if (max != null) maximum = max.value();

        int val = -1;
        int count = 0;
        while (val < minimum || val > maximum) {
            val = poisson.sample();
            count += 1;
            if (count > MAX_TRIES) {
                throw new RuntimeException("Failed to draw conditional Poisson random variable after " + MAX_TRIES + " attempts.");
            }
        }


        return new RandomVariable<>(null, val, this);
    }

    public double density(Integer i) {
        PoissonDistribution poisson = new PoissonDistribution(doubleValue(lambda));
        return poisson.probability(i);
    }

    @Override
    public Map<String,Value> getParams() {
        return new TreeMap<>() {{
            put(lambdaParamName, lambda);
            if (min != null) put(minParamName, min);
            if (max != null) put(maxParamName, max);
        }};    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case lambdaParamName:
                lambda = value;
                break;
            case minParamName:
                min = value;
                break;
            case maxParamName:
                max = value;
                break;
            default:
                throw new RuntimeException("The valid parameter names are " + lambdaParamName + ", " + minParamName + " and " + maxParamName);
        }
    }

    public void setLambda(double p) {
        this.lambda.setValue(p);
    }

    public String toString() {
        return getName();
    }

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
}
