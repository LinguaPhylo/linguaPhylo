package lphy.core.distributions;

import lphy.graphicalModel.*;
import lphy.util.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.lowerParamName;
import static lphy.core.distributions.DistributionConstants.upperParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Uniform implements GenerativeDistribution<Double> {

    private Value<Number> lower;
    private Value<Number> upper;

    private RandomGenerator random;

    public Uniform(@ParameterInfo(name = lowerParamName, description = "the lower bound of the uniform distribution.") Value<Number> lower,
                   @ParameterInfo(name = upperParamName, description = "the upper bound of the uniform distribution.") Value<Number> upper) {

        this.lower = lower;
        this.upper = upper;
        this.random = RandomUtils.getRandom();
    }

    @GeneratorInfo(name = "Uniform",
            category = GeneratorCategory.PROB_DIST, examples = {"simFossilsCompact.lphy"},
            description = "The uniform probability distribution.")
    public RandomVariable<Double> sample() {

        double l = doubleValue(lower);
        double u = doubleValue(upper);

        double x = random.nextDouble() * (u - l) + l;

        return new RandomVariable<Double>(null, x, this);
    }

    public double logDensity(Double x) {
        if (x < doubleValue(lower) || x > doubleValue(upper)) return Double.NEGATIVE_INFINITY;
        return Math.log(1.0) - Math.log(doubleValue(upper) - doubleValue(lower));
    }

    public double density(Double x) {
        if (x < doubleValue(lower) || x > doubleValue(upper)) return 0.0;
        return 1.0 / (doubleValue(upper) - doubleValue(lower));
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

    public Value<Number> getLower() {
        return lower;
    }

    public Value<Number> getUpper() {
        return upper;
    }
}