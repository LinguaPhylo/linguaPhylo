package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

/**
 * Created by adru001 on 18/12/19.
 */
public class Geometric implements GenerativeDistribution<Integer> {

    private final String pParamName;
    private Value<Double> p;

    private RandomGenerator random;

    public Geometric(@ParameterInfo(name="p", description="the probability of success.") Value<Double> p) {
        this.p = p;
        this.random = Utils.getRandom();
        pParamName = getParamName(0);
    }

    @GenerativeDistributionInfo(name="Gamma", description="The probability distribution of the number of failures before the first success given a fixed probability of success p, supported on the set { 0, 1, 2, 3, ... }.")
    public RandomVariable<Integer> sample() {

        GeometricDistribution geom = new GeometricDistribution(p.value());
        return new RandomVariable<>("x", geom.sample(), this);
    }

    public double density(Integer i) {
        GeometricDistribution geom = new GeometricDistribution(p.value());
        return geom.probability(i);
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
