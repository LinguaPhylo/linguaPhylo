package lphy.core.distributions;

import lphy.graphicalModel.*;
import lphy.util.RandomUtils;
import org.apache.commons.math3.distribution.CauchyDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.meanParamName;
import static lphy.core.distributions.DistributionConstants.scaleParamName;

/**
 * Gamma distribution
 */
public class Cauchy implements GenerativeDistribution<Double> {

    private Value<Double> mean;
    private Value<Double> scale;

    CauchyDistribution cauchyDistribution;

    public Cauchy(@ParameterInfo(name = meanParamName, description = "the mean of the Cauchy distribution.") Value<Double> mean,
                  @ParameterInfo(name = scaleParamName, description = "the scale of the Cauchy distribution.") Value<Double> scale) {

        this.mean = mean;
        if (mean == null) throw new IllegalArgumentException("The mean value can't be null!");
        this.scale = scale;
        if (scale == null) throw new IllegalArgumentException("The scale value can't be null!");

        constructCauchyDistribution();
    }

    @GeneratorInfo(name = "Gamma", description = "The gamma probability distribution.")
    public RandomVariable<Double> sample() {
        constructCauchyDistribution();
        double x = cauchyDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return cauchyDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanParamName, mean);
            put(scaleParamName, scale);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) mean = value;
        else if (paramName.equals(scaleParamName)) scale = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        constructCauchyDistribution();
    }

    private void constructCauchyDistribution() {
        // in case the shape is type integer
        double mean = ((Number) this.mean.value()).doubleValue();

        // in case the scale is type integer
        double sc = ((Number) scale.value()).doubleValue();

        cauchyDistribution = new CauchyDistribution(RandomUtils.getRandom(), mean, sc);
    }

    public String toString() {
        return getName();
    }
}