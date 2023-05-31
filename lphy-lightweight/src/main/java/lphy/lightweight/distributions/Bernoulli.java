package lphy.base.lightweight.distributions;

import lphy.base.lightweight.LGenerativeDistribution;
import lphy.core.graphicalmodel.components.GeneratorInfo;
import lphy.core.graphicalmodel.components.ParameterInfo;
import lphy.core.util.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Bernoulli implements LGenerativeDistribution<Boolean> {

    Double p;
    RandomGenerator randomGenerator;

    public Bernoulli(@ParameterInfo(name="p", description="the probability of success.") Double p) {
        this.p = p;
        this.randomGenerator = RandomUtils.getRandom();
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
