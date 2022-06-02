package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.medianParamName;
import static lphy.core.distributions.DistributionConstants.scaleParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Cauchy distribution
 */
public class Cauchy extends PriorDistributionGenerator<Double> {

    private Value<Number> median;
    private Value<Number> scale;

    CauchyDistribution cauchyDistribution;

    public Cauchy(@ParameterInfo(name = medianParamName, description = "the median of the Cauchy distribution.") Value<Number> median,
                  @ParameterInfo(name = scaleParamName, description = "the scale of the Cauchy distribution.") Value<Number> scale) {
        super();
        this.median = median;
        this.scale = scale;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (median == null) throw new IllegalArgumentException("The median value can't be null!");
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");
        // in case type integer
        cauchyDistribution = new CauchyDistribution(random, doubleValue(median), doubleValue(scale));
    }

    @GeneratorInfo(name = "Cauchy", verbClause = "has", narrativeName = "Cauchy distribution prior",
            category = GeneratorCategory.PROB_DIST, description = "The Cauchy distribution.")
    public RandomVariable<Double> sample() {
        double x = cauchyDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return cauchyDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(medianParamName, median);
            put(scaleParamName, scale);
        }};
    }

}