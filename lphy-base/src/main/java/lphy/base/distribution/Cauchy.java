package lphy.base.distribution;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.model.component.RandomVariable;
import lphy.core.model.component.Value;
import lphy.core.model.component.ValueUtils;
import lphy.core.model.component.argument.ParameterInfo;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

/**
 * Cauchy distribution.
 * @see CauchyDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Cauchy extends ParametricDistribution<Double> {

    private Value<Number> median;
    private Value<Number> scale;

    CauchyDistribution cauchyDistribution;

    public Cauchy(@ParameterInfo(name = DistributionConstants.medianParamName, description = "the median of the Cauchy distribution.") Value<Number> median,
                  @ParameterInfo(name = DistributionConstants.scaleParamName, description = "the scale of the Cauchy distribution.") Value<Number> scale) {
        super();
        this.median = median;
        this.scale = scale;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (median == null) throw new IllegalArgumentException("The median value can't be null!");
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");
        // use code available since apache math 3.1
        cauchyDistribution = new CauchyDistribution(random, ValueUtils.doubleValue(median), ValueUtils.doubleValue(scale),
                CauchyDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "Cauchy", verbClause = "has", narrativeName = "Cauchy distribution prior",
            category = GeneratorCategory.PRIOR, description = "The Cauchy distribution.")
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
            put(DistributionConstants.medianParamName, median);
            put(DistributionConstants.scaleParamName, scale);
        }};
    }

}