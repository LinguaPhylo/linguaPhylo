package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

import static lphy.core.distributions.DistributionConstants.*;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by adru001 on 18/12/19.
 */
public class Bernoulli implements GenerativeDistribution<Boolean> {
    private Value<Number> p;

    private RandomGenerator random;

    public Bernoulli(@ParameterInfo(name=pParamName, description="the probability of success.", type=Number.class) Value<Number> p) {
        this.p = p;
        this.random = Utils.getRandom();
    }

    @GeneratorInfo(name="Bernoulli", description="The coin toss distribution. With true (heads) having probability p.", returnType = Boolean.class)
    public RandomVariable<Boolean> sample() {

        boolean success = (random.nextDouble() < doubleValue(p));
        return new RandomVariable<>("x", success, this);
    }

    public double density(Boolean success) {
        return success ? doubleValue(p) : (1.0 - doubleValue(p));
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

    public void setP(Double p) {
        this.p.setValue(p);
    }

    public String toString() {
        return getName();
    }
}
