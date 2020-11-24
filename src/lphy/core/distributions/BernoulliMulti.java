package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.*;

/**
 * A Bernoulli process of n trials.
 */
public class BernoulliMulti implements GenerativeDistribution<Boolean[]> {
    private Value<Double> p;
    private Value<Integer> n;

    private RandomGenerator random;

    public BernoulliMulti(@ParameterInfo(name = pParamName, description = "the probability of success.") Value<Double> p,
                          @ParameterInfo(name = nParamName, description = "the number of bernoulli trials.") Value<Integer> n) {
        this.p = p;
        this.n = n;
        this.random = Utils.getRandom();
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
        return new TreeMap<>() {{
            put(pParamName, p);
            put(nParamName, n);
        }};
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
