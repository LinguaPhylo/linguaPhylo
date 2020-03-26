package lphy.core.lightweight.distributions;

import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.PoissonDistribution;

/**
 * Created by adru001 on 18/12/19.
 */
public class Poisson implements LightweightGenerativeDistribution<Integer> {

    private Double lambda;

    PoissonDistribution poissonDistribution;

    public Poisson(@ParameterInfo(name="lambda", description="the expected number of events.") Double lambda) {
        this.lambda = lambda;
        constructPoissonDistribution();
    }

    private void constructPoissonDistribution() {
        poissonDistribution = new PoissonDistribution(lambda);
    }

    @GeneratorInfo(name="Poisson", description="The probability distribution of the number of events when the expected number of events is lambda, supported on the set { 0, 1, 2, 3, ... }.")
    public Integer sample() {

        return poissonDistribution.sample();
    }

    public double density(Integer i) {
        PoissonDistribution poisson = new PoissonDistribution(lambda);
        return poisson.probability(i);
    }

    public Double getLambda() {
        return lambda;
    }

    public void setLambda(Double lambda) {
        constructPoissonDistribution();
        this.lambda = lambda;
    }

    public String toString() {
        return getName();
    }
}
