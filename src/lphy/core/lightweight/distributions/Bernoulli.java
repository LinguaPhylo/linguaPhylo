package lphy.core.lightweight.distributions;

import lphy.core.distributions.Utils;
import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by adru001 on 18/12/19.
 */
public class Bernoulli implements LightweightGenerativeDistribution<Boolean> {

    Double p;
    RandomGenerator randomGenerator;

    public Bernoulli(@ParameterInfo(name="p", description="the probability of success.") Double p) {
        this.p = p;
        this.randomGenerator = Utils.getRandom();
    }

    @GeneratorInfo(name="Bernoulli", description="The coin toss distribution. With true (heads) having probability p.")
    public Boolean sample() {
        return randomGenerator.nextDouble() <= p;
    }

    public double density(Boolean i) {
        return i ? p : (1.0 - p);
    }

    public void setP(Double p) {
        this.p = p;
    }

    public Double getP() {
        return p;
    }
}
