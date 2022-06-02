package lphy.core.distributions;

import lphy.graphicalModel.*;
import lphy.util.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.lowerParamName;
import static lphy.core.distributions.DistributionConstants.upperParamName;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class DiscreteUniform implements GenerativeDistribution<Integer> {

    private Value<Integer> lower;
    private Value<Integer> upper;

    private RandomGenerator random;

    public DiscreteUniform(@ParameterInfo(name = lowerParamName, description = "the lower bound (inclusive) of the uniform distribution on integers.") Value<Integer> lower,
                           @ParameterInfo(name = upperParamName, description = "the upper bound (inclusive) of the uniform distribution on integer.") Value<Integer> upper) {

        this.lower = lower;
        this.upper = upper;
        this.random = RandomUtils.getRandom();
    }

    @GeneratorInfo(name = "DiscreteUniform",
            category = GeneratorCategory.PROB_DIST, examples = {"simpleBModelTest.lphy","simpleBModelTest2.lphy"},
            description = "The discrete uniform distribution over integers.")
    public RandomVariable<Integer> sample() {

        int l = lower.value();
        int u = upper.value();

        int x = random.nextInt(u-l+1) + l;

        return new RandomVariable<>(null, x, this);
    }

    public double logDensity(Integer x) {
        if (x < lower.value() || x > upper.value()) return Double.NEGATIVE_INFINITY;
        return Math.log(1.0) - Math.log(upper.value() - lower.value());
    }

    public double density(Double x) {
        if (x < lower.value() || x > upper.value()) return 0.0;
        return 1.0 / (upper.value() - lower.value() + 1);
    }
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(lowerParamName, lower);
            put(upperParamName, upper);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(lowerParamName)) lower = value;
        else if (paramName.equals(upperParamName)) upper = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

    public Value<Integer> getLower() {
        return lower;
    }

    public Value<Integer> getUpper() {
        return upper;
    }
}