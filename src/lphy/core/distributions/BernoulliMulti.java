package lphy.core.distributions;

import lphy.graphicalModel.*;
import lphy.utils.LoggerUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.nParamName;
import static lphy.core.distributions.DistributionConstants.pParamName;

/**
 * A Bernoulli process of n trials.
 */
public class BernoulliMulti implements GenerativeDistribution<Boolean[]> {
    private Value<Double> p;
    private Value<Integer> n;
    private Value<Integer> minHammingWeight;

    private RandomGenerator random;

    public final String minHammingWeightParamName = "minHammingWeight";

    private static final int MAX_TRIES = 1000;

    public BernoulliMulti(@ParameterInfo(name = pParamName, description = "the probability of success.") Value<Double> p,
                          @ParameterInfo(name = nParamName, description = "the number of bernoulli trials.") Value<Integer> n,
                          @ParameterInfo(name = minHammingWeightParamName, description = "Optional condition: the minimum number of ones in the boolean array.", optional = true) Value<Integer> minHammingWeight) {
        this.p = p;
        this.n = n;
        this.minHammingWeight = minHammingWeight;
        this.random = Utils.getRandom();
    }

    @GeneratorInfo(name = "Bernoulli", description = "The Bernoulli process for n iid trials. The success (true) probability is p. Produces a boolean n-tuple.")
    public RandomVariable<Boolean[]> sample() {

        Boolean[] b = bernoulli(p.value(), n.value());
        if (minHammingWeight != null) {
            int tries = 0;
            while (hammingWeight(b) < minHammingWeight.value() && tries < MAX_TRIES) {
                b = bernoulli(p.value(), n.value());
                tries += 1;
            }
            if (tries == MAX_TRIES) {
                RandomBooleanArray booleanArray = new RandomBooleanArray(n, minHammingWeight);
                b = booleanArray.sample().value();
                LoggerUtils.log.severe("Bernoulli failed to generate minimum hamming weight after " + MAX_TRIES + " so returned RandomBooleanArray with minimum hamming weight.");
            }
        }

        return new RandomVariable<>(null, b, this);
    }

    private int hammingWeight(Boolean[] b) {
        int sum = 0;
        for (Boolean i : b) {
            if (i) sum += 1;
        }
        return sum;
    }

    private Boolean[] bernoulli(double p, int n) {
        Boolean[] successes = new Boolean[n];
        for (int i = 0; i < successes.length; i++) {
            successes[i] = (random.nextDouble() < p);
        }
        return successes;
    }

    public double logDensity(Boolean[] successes) {
        double logP = 0.0;

        double lnp = Math.log(p.value());
        double ln1mp = Math.log(1.0 - p.value());

        for (int i = 0; i < successes.length; i++) {
            logP += successes[i] ? lnp : ln1mp;
        }
        return logP;
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(pParamName, p);
            put(nParamName, n);
            if (minHammingWeight != null) put(minHammingWeightParamName, minHammingWeight);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case pParamName:
                p = value;
                break;
            case nParamName:
                n = value;
                break;
            case minHammingWeightParamName:
                minHammingWeight = value;
                break;
            default:
                throw new RuntimeException("Expected " + pParamName + " or " + nParamName + " or " + minHammingWeightParamName);
        }
    }

    public void setSuccessProbability(double p) {
        this.p.setValue(p);
    }

    public String toString() {
        return getName();
    }

    public Value<Double> getP() {
        return getParams().get(pParamName);
    }

    public Value<Integer> getMinHammingWeight() {
        return getParams().get(minHammingWeightParamName);
    }
}
