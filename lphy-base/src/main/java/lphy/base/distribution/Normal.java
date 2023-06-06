package lphy.base.distribution;

import lphy.base.math.RandomUtils;
import lphy.core.model.*;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.parser.argument.ParameterInfo;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.meanParamName;
import static lphy.base.distribution.DistributionConstants.sdParamName;

/**
 * Normal distribution prior.
 * @see NormalDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Normal extends ParametricDistribution<Double> implements GenerativeDistribution1D<Double> {

    private Value<Number> mean;
    private Value<Number> sd;

    NormalDistribution normalDistribution;

    public Normal(@ParameterInfo(name = "mean", description = "the mean of the distribution.") Value<Number> mean,
                  @ParameterInfo(name = "sd", narrativeName = "standard deviation", description = "the standard deviation of the distribution.") Value<Number> sd) {
        super();
        this.mean = mean;
        this.sd = sd;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (mean == null) throw new IllegalArgumentException("The mean value can't be null!");
        if (sd == null) throw new IllegalArgumentException("The sd value can't be null!");

        normalDistribution = new NormalDistribution(RandomUtils.getRandom(), ValueUtils.doubleValue(mean), ValueUtils.doubleValue(sd),
                NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "Normal", verbClause = "has", narrativeName = "normal prior",
            category = GeneratorCategory.PRIOR, examples = {"simplePhyloBrownian.lphy","simplePhyloOU.lphy"},
            description = "The normal probability distribution.")
    public RandomVariable<Double> sample() {
        // constructDistribution() only required in constructor and setParam
        double x = normalDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return normalDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanParamName, mean);
            put(sdParamName, sd);
        }};
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) mean = value;
        else if (paramName.equals(sdParamName)) sd = value;
        else super.setParam(paramName, value);
    }

    public Value<Number> getMean() {
        return mean;
    }

    public Value<Number> getSd() {
        return sd;
    }

    private static final Double[] domainBounds = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}