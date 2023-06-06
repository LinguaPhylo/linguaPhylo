package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.lowerParamName;
import static lphy.base.distribution.DistributionConstants.upperParamName;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class UniformDiscrete extends ParametricDistribution<Integer> {

    private Value<Integer> lower;
    private Value<Integer> upper;

    public UniformDiscrete(@ParameterInfo(name = lowerParamName, description = "the lower bound (inclusive) of the uniform distribution on integers.") Value<Integer> lower,
                           @ParameterInfo(name = upperParamName, description = "the upper bound (inclusive) of the uniform distribution on integer.") Value<Integer> upper) {
        super();
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {  }

    @GeneratorInfo(name = "UniformDiscrete",
            category = GeneratorCategory.PRIOR, examples = {"simpleBModelTest.lphy","simpleBModelTest2.lphy"},
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

    public Value<Integer> getLower() {
        return lower;
    }

    public Value<Integer> getUpper() {
        return upper;
    }
}