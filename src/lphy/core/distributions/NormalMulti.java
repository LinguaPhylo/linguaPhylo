package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.*;

/**
 * Created by adru001 on 18/12/19.
 */
@Deprecated()
public class NormalMulti implements GenerativeDistribution<Double[]> {

    private Value<Double> mean;
    private Value<Double> sd;
    private Value<Integer> n;

    private RandomGenerator random;

    NormalDistribution normalDistribution;

    public NormalMulti(@ParameterInfo(name = meanParamName, description = "the mean of the distribution.") Value<Double> mean,
                       @ParameterInfo(name = sdParamName, description = "the standard deviation of the distribution.") Value<Double> sd,
                       @ParameterInfo(name = nParamName, description = "the dimension of the return.") Value<Integer> n) {

        this.mean = mean;
        this.sd = sd;
        this.n = n;
        this.random = Utils.getRandom();
    }

    @GeneratorInfo(name = "Normal", description = "The normal probability distribution.")
    public RandomVariable<Double[]> sample() {
        update();

        Double[] result = new Double[n.value()];
        for (int i = 0; i < result.length; i++) {
            result[i] = normalDistribution.inverseCumulativeProbability(Utils.getRandom().nextDouble());
        }

        return new RandomVariable<>("x", result, this);
    }

    public double logDensity(Double[] x) {

        double logDensity = 0;
        for (int i = 0; i < x.length; i++) {
            logDensity += normalDistribution.logDensity(x[i]);
        }
        return logDensity;
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanParamName, mean);
            put(sdParamName, sd);
            put(nParamName, n);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case meanParamName:
                mean = value;
                break;
            case sdParamName:
                sd = value;
                break;
            case nParamName:
                n = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }

        update();
    }

    public String toString() {
        return getName();
    }

    private void update() {
        normalDistribution = new NormalDistribution(mean.value(), sd.value());
    }

    public Value<Double> getMean() {
        return mean;
    }

    public Value<Double> getSd() {
        return sd;
    }
}