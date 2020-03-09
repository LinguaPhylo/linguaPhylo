package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

/**
 * Created by adru001 on 18/12/19.
 */
public class Bernoulli implements GenerativeDistribution<Boolean> {
    private final String pParamName;
    private Value<Double> p;

    private RandomGenerator random;

    public Bernoulli(@ParameterInfo(name="p", description="the probability of success.") Value<Double> p) {
        this.p = p;
        this.random = Utils.getRandom();
        pParamName = getParamName(0);
    }

    @GenerativeDistributionInfo(name="Bernoulli", description="The coin toss distribution. With true (heads) having probability p.")
    public RandomVariable<Boolean> sample() {

        return new RandomVariable<>("x", random.nextBoolean(), this);
    }

    public double density(Boolean i) {
        return i ? p.value() : (1.0 - p.value());
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(getParamName(0), p);
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(pParamName)) {
            p = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + pParamName);
        }
    }

    public void setSuccessProbability(double p) {
        this.p.setValue(p);
    }

    public String toString() {
        return getName();
    }
}
