package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A Bernoulli process of n trials.
 */
public class BernoulliMulti implements GenerativeDistribution<Boolean[]> {
    private final String pParamName;
    private Value<Double> p;
    private final String nParamName;
    private Value<Integer> n;

    private RandomGenerator random;

    public BernoulliMulti(@ParameterInfo(name = "p", description = "the probability of success.", type = Double.class) Value<Double> p,
                          @ParameterInfo(name = "n", description = "the number of bernoulli trials.", type = Integer.class) Value<Integer> n) {
        this.p = p;
        this.n = n;
        this.random = Utils.getRandom();
        pParamName = getParamName(0);
        nParamName = getParamName(1);
    }

    @GeneratorInfo(name = "Bernoulli", description = "The Bernoulli process for n iid trials. The success (true) probability is p. Produces a boolean n-tuple.")
    public RandomVariable<Boolean[]> sample() {

        Boolean[] successes = new Boolean[n.value()];
        for (int i = 0; i < successes.length; i++) {
            successes[i] = (random.nextDouble() < p.value());
        }
        return new RandomVariable<>("x", successes, this);
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
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(pParamName, p);
        map.put(nParamName, n);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(pParamName)) {
            p = value;
        } else if (paramName.equals(nParamName)) {
            n = value;
        } else {
            throw new RuntimeException("Expected either " + pParamName + " or " + nParamName);
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
}
