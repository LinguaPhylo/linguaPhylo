package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class Exp implements GenerativeDistribution<Double> {

    private final String rateParamName;
    private Value<Double> rate;

    private RandomGenerator random;

    public Exp(@ParameterInfo(name="rate", description="the rate of an exponential distribution.") Value<Double> rate) {
        this.rate = rate;
        this.random = Utils.getRandom();
        rateParamName = getParamName(0);
    }

    @GenerativeDistributionInfo(name="Exp", description="The exponential probability distribution.")
    public RandomVariable<Double> sample() {

        double x = - Math.log(random.nextDouble()) / rate.value();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double aDouble) {
        return 0;
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(getParamName(0), rate);
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(rateParamName)) {
            rate = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + rateParamName);
        }
    }

    public void setRate(double rate) {
        this.rate.setValue(rate);
    }

    public String toString() {
        return getName();
    }
}
