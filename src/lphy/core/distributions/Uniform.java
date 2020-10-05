package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class Uniform implements GenerativeDistribution<Double> {

    private Value<Double> lower;
    private Value<Double> upper;

    private RandomGenerator random;

    public Uniform(@ParameterInfo(name = lowerParamName, description = "the lower bound of the uniform distribution.") Value<Double> lower,
                   @ParameterInfo(name = upperParamName, description = "the upper bound of the uniform distribution.") Value<Double> upper) {

        this.lower = lower;
        this.upper = upper;
        this.random = Utils.getRandom();
    }

    @GeneratorInfo(name = "Uniform", description = "The uniform probability distribution.")
    public RandomVariable<Double> sample() {

        double l = lower.value();
        double u = upper.value();

        double x = random.nextDouble() * (u - l) + l;

        return new RandomVariable<Double>(null, x, this);
    }

    public double logDensity(Double x) {
        return Math.log(1.0) - Math.log(upper.value() - lower.value());
    }

    public double density(Double x) {
        return 1.0 / upper.value() - lower.value();
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
}