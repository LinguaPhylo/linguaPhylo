package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class Exp implements GenerativeDistribution<Double> {

    private final String meanParamName;
    private Value<Double> mean;

    private RandomGenerator random;

    public Exp(@ParameterInfo(name="mean", description="the mean of an exponential distribution.") Value<Double> mean) {
        this.mean = mean;
        this.random = Utils.getRandom();
        meanParamName = getParamName(0);
    }

    @GeneratorInfo(name="Exp", description="The exponential probability distribution.")
    public RandomVariable<Double> sample() {

        double x = - Math.log(random.nextDouble()) * mean.value();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double aDouble) {
        return 0;
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(getParamName(0), mean);
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) {
            mean = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + meanParamName);
        }
    }

    public void setMean(double mean) {
        this.mean.setValue(mean);
    }

    public String toString() {
        return getName();
    }
}
