package lphy.core.lightweight.distributions;

import lphy.core.distributions.Utils;
import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by adru001 on 18/12/19.
 */
public class Exp implements LightweightGenerativeDistribution<Double> {

    private Double rate;

    private RandomGenerator random;

    public Exp(@ParameterInfo(name="rate", description="the rate of an exponential distribution.") Double rate) {
        this.rate = rate;
        this.random = Utils.getRandom();
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
