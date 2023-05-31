package lphy.base.lightweight.distributions;

import lphy.base.lightweight.LGenerativeDistribution;
import lphy.core.graphicalmodel.components.GeneratorInfo;
import lphy.core.graphicalmodel.components.ParameterInfo;
import lphy.core.util.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Exp implements LGenerativeDistribution<Double> {

    private Double rate;

    private RandomGenerator random;

    public Exp(@ParameterInfo(name="rate", description="the rate of an exponential distribution.") Double rate) {
        this.rate = rate;
        this.random = RandomUtils.getRandom();
    }

    @GeneratorInfo(name="Exp", description="The exponential probability distribution.")
    public Double sample() {

       return - Math.log(random.nextDouble()) / rate;
    }

    public double density(Double aDouble) {
        return 0;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String toString() {
        return getName();
    }
}
