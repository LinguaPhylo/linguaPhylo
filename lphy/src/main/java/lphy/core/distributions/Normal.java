package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.meanParamName;
import static lphy.core.distributions.DistributionConstants.sdParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Normal distribution
 */
public class Normal implements GenerativeDistribution1D<Double> {

    private Value<Number> mean;
    private Value<Number> sd;

    NormalDistribution normalDistribution;

    public Normal(@ParameterInfo(name = "mean", description = "the mean of the distribution.") Value<Number> mean,
                  @ParameterInfo(name = "sd", narrativeName = "standard deviation", description = "the standard deviation of the distribution.") Value<Number> sd) {

        this.mean = mean;
        this.sd = sd;

        constructDistribution();
    }

    @GeneratorInfo(name = "Normal", verbClause = "has", narrativeName = "normal prior",
            category = GeneratorCategory.PROB_DIST, examples = {"simplePhyloBrownian.lphy","simplePhyloOU.lphy"},
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

    @Override
    public void constructDistribution() {
        if (mean == null) throw new IllegalArgumentException("The mean value can't be null!");
        if (sd == null) throw new IllegalArgumentException("The sd value can't be null!");

        normalDistribution = new NormalDistribution(Utils.getRandom(), doubleValue(mean), doubleValue(sd));
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanParamName, mean);
            put(sdParamName, sd);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (meanParamName.equals(paramName)) {
            mean = value;
        } else if (sdParamName.equals(paramName)) {
            sd = value;
        } else {
            throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
        constructDistribution();
    }

    public String toString() {
        return getName();
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