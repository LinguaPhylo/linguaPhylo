package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.lowerParamName;
import static lphy.core.distributions.DistributionConstants.upperParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Uniform extends PriorDistributionGenerator<Double> implements GenerativeDistribution1D<Double> {

    private Value<Number> lower;
    private Value<Number> upper;

    public Uniform(@ParameterInfo(name = lowerParamName, description = "the lower bound of the uniform distribution.") Value<Number> lower,
                   @ParameterInfo(name = upperParamName, description = "the upper bound of the uniform distribution.") Value<Number> upper) {
        super();
        this.lower = lower;
        this.upper = upper;
//        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
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

    private static final Double[] domainBounds = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
    @Override
    public Double[] getDomainBounds() {
        return domainBounds;
    }

    public Value<Number> getLower() {
        return lower;
    }

    public Value<Number> getUpper() {
        return upper;
    }
}