package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 18/12/19.
 */
public class ExpMarkovChain implements GenerativeDistribution<Double[]> {

    private final String initialMeanParamName;
    private final String nParamName;
    private Value<Double> initialMean;
    private Value<Integer> n;

    private RandomGenerator random;

    public ExpMarkovChain(@ParameterInfo(name = "initialMean", description = "the initial value. This is the mean of the exponential from which the first value of the chain is drawn.") Value<Double> initialMean,
                          @ParameterInfo(name = "n", description = "the dimension of the return. X[0] ~ Exp(mean=initialMean); X[i+1] ~ Exp(mean=X[i])") Value<Integer> n) {

        this.initialMean = initialMean;
        this.n = n;
        this.random = Utils.getRandom();

        initialMeanParamName = getParamName(0);
        nParamName = getParamName(1);
    }

    @GeneratorInfo(name="ExpMarkovChain", description="A chain of random variables. X[0] ~ Exp(mean=initialMean); X[i+1] ~ Exp(mean=X[i])")
    public RandomVariable<Double[]> sample() {

        ExponentialDistribution exp = new ExponentialDistribution(initialMean.value());
        Double[] result = new Double[n.value()];
        for (int i = 0; i < result.length; i++) {
            result[i] = exp.sample();
            exp = new ExponentialDistribution(result[i]);
        }

        return new RandomVariable<>("x", result, this);
    }

    public double logDensity(Double[] x) {

        double logDensity = 0;
        ExponentialDistribution exp = new ExponentialDistribution(initialMean.value());
        for (int i = 0; i < x.length; i++) {
            logDensity += exp.logDensity(x[i]);
            exp = new ExponentialDistribution(x[i]);
        }
        return logDensity;
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(initialMeanParamName, initialMean);
        map.put(nParamName, n);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(initialMeanParamName)) initialMean = value;
        else if (paramName.equals(nParamName)) n = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

    public Value<Double> getInitialMean() {
        return initialMean;
    }

    public Value<Integer> getN() {
        return n;
    }
}