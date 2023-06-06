package lphy.base.distribution;

import lphy.core.model.*;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Uniform extends ParametricDistribution<Double> implements GenerativeDistribution1D<Double> {

    private Value<Number> lower;
    private Value<Number> upper;

    public Uniform(@ParameterInfo(name = DistributionConstants.lowerParamName, description = "the lower bound of the uniform distribution.") Value<Number> lower,
                   @ParameterInfo(name = DistributionConstants.upperParamName, description = "the upper bound of the uniform distribution.") Value<Number> upper) {
        super();
        this.lower = lower;
        this.upper = upper;
//        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "Uniform",
            category = GeneratorCategory.PRIOR, examples = {"simFossilsCompact.lphy"},
            description = "The uniform probability distribution.")
    public RandomVariable<Double> sample() {

        double l = ValueUtils.doubleValue(lower);
        double u = ValueUtils.doubleValue(upper);

        double x = random.nextDouble() * (u - l) + l;

        return new RandomVariable<Double>(null, x, this);
    }

    public double logDensity(Double x) {
        if (x < ValueUtils.doubleValue(lower) || x > ValueUtils.doubleValue(upper)) return Double.NEGATIVE_INFINITY;
        return Math.log(1.0) - Math.log(ValueUtils.doubleValue(upper) - ValueUtils.doubleValue(lower));
    }

    public double density(Double x) {
        if (x < ValueUtils.doubleValue(lower) || x > ValueUtils.doubleValue(upper)) return 0.0;
        return 1.0 / (ValueUtils.doubleValue(upper) - ValueUtils.doubleValue(lower));
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.lowerParamName, lower);
            put(DistributionConstants.upperParamName, upper);
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