package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GeometricDistribution;

import java.util.Collections;
import java.util.Map;

import static lphy.core.distributions.DistributionConstants.pParamName;

/**
 * Created by adru001 on 18/12/19.
 */
public class Geometric implements GenerativeDistribution1D<Integer> {

    private Value<Double> p;

    public Geometric(@ParameterInfo(name=pParamName, description="the probability of success.") Value<Double> p) {
        this.p = p;
    }

    @GeneratorInfo(name="Gamma", description="The probability distribution of the number of failures before the first success given a fixed probability of success p, supported on the set { 0, 1, 2, 3, ... }.")
    public RandomVariable<Integer> sample() {

        GeometricDistribution geom = new GeometricDistribution(p.value());
        return new RandomVariable<>(null, geom.sample(), this);
    }

    public double density(Integer i) {
        GeometricDistribution geom = new GeometricDistribution(p.value());
        return geom.probability(i);
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(pParamName, p);
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

    private static final Integer[] domainBounds = {0, Integer.MAX_VALUE};
    public Integer[] getDomainBounds() {
        return domainBounds;
    }
}
