package lphy.base.distributions;

import lphy.base.math.RandomUtils;
import lphy.core.model.components.*;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

@Deprecated()
public class ExpMulti implements GenerativeDistribution<Double[]> {

    private Value<Double> mean;
    private Value<Integer> n;


    private RandomGenerator random;

    public ExpMulti(@ParameterInfo(name = DistributionConstants.meanParamName, description = "the mean of an exponential distribution.") Value<Double> mean,
                    @ParameterInfo(name = DistributionConstants.nParamName, description = "the number of iid exponential draws.") Value<Integer> n) {
        this.mean = mean;
        this.n = n;

        this.random = RandomUtils.getRandom();
    }

    @GeneratorInfo(name = "Exp", description = "The exponential probability distribution.")
    public RandomVariable<Double[]> sample() {

        Double[] x = new Double[n.value()];

        for (int i = 0; i < x.length; i++) {
            x[i] = -Math.log(random.nextDouble()) * getMean();
        }
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double logDensity(Double[] x) {
        ExponentialDistribution exp = new ExponentialDistribution(random, mean.value());
        double logP = exp.logDensity(x[0]);
        for (int i = 1; i < x.length; i++) {
            logP += exp.logDensity(x[i]);
        }
        return logP;
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.meanParamName, mean);
            put(DistributionConstants.nParamName, n);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (DistributionConstants.meanParamName.equals(paramName)) {
            mean = value;
        } else if (DistributionConstants.nParamName.equals(paramName)) {
            n = value;
        } else {
            throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }

    public double getMean() {
        if (mean != null) return mean.value();
        return 1.0;
    }

    public void setMean(double mean) {
        this.mean.setValue(mean);
    }

    public String toString() {
        return getName();
    }
}
