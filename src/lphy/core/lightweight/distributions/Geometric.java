package lphy.core.lightweight.distributions;

import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by adru001 on 18/12/19.
 */
public class Geometric implements LightweightGenerativeDistribution<Integer> {

    private Double p;

    private RandomGenerator random;

    public Geometric(@ParameterInfo(name="p", description="the probability of success.") Double p) {
        this.p = p;
    }

    @GeneratorInfo(name="Geometric", description="The probability distribution of the number of failures before the first success given a fixed probability of success p, supported on the set { 0, 1, 2, 3, ... }.")
    public Integer sample() {

        GeometricDistribution geom = new GeometricDistribution(p);
        return geom.sample();
    }

    public double density(Integer i) {
        GeometricDistribution geom = new GeometricDistribution(p);
        return geom.probability(i);
    }

    public Double getP() {
        return p;
    }

    public void setP(Double p) {
        this.p = p;
    }

    public String toString() {
        return getName();
    }
}
