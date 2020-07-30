package lphy.core.distributions;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.Normal;
import lphy2beast.BEASTContext;
import lphy.graphicalModel.*;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 18/12/19.
 */
public class NormalMulti implements GenerativeDistribution<Double[]> {

    private final String meanParamName;
    private final String sdParamName;
    private final String nParamName;
    private Value<Double> mean;
    private Value<Double> sd;
    private Value<Integer> n;

    private RandomGenerator random;

    NormalDistribution normalDistribution;

    public NormalMulti(@ParameterInfo(name = "mean", description = "the mean of the distribution.") Value<Double> mean,
                       @ParameterInfo(name = "sd", description = "the standard deviation of the distribution.") Value<Double> sd,
                       @ParameterInfo(name = "n", description = "the dimension of the return.") Value<Integer> n) {

        this.mean = mean;
        this.sd = sd;
        this.n = n;
        this.random = Utils.getRandom();

        meanParamName = getParamName(0);
        sdParamName = getParamName(1);
        nParamName = getParamName(2);
    }

    @GeneratorInfo(name="Normal", description="The normal probability distribution.")
    public RandomVariable<Double[]> sample() {
        update();

        Double[] result = new Double[n.value()];
        for (int i = 0; i < result.length; i++) {
            try {
                result[i] = normalDistribution.inverseCumulativeProbability(Utils.getRandom().nextDouble());
            } catch (MathException e) {
                throw new RuntimeException("Math exception while drawing a random normal variate.");
            }
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
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(meanParamName, mean);
        map.put(sdParamName, sd);
        map.put(nParamName, n);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) mean = value;
        else if (paramName.equals(sdParamName)) sd = value;
        else if (paramName.equals(nParamName)) n = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        update();
    }

    public String toString() {
        return getName();
    }

    private void update() {
        normalDistribution = new NormalDistributionImpl(mean.value(), sd.value());
    }

    public Value<Double> getMean() {
        return mean;
    }

    public Value<Double> getSd() {
        return sd;
    }
}