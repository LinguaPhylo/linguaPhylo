package lphy.base.lightweight.distributions;

import lphy.base.lightweight.LGenerativeDistribution;
import lphy.core.model.component.GeneratorInfo;
import lphy.core.model.component.ParameterInfo;
import org.apache.commons.math3.distribution.BinomialDistribution;

public class Binomial implements LGenerativeDistribution<Integer> {

    private Double p;
    private Integer n;
    BinomialDistribution binomial;

    public Binomial(@ParameterInfo(name="prob", description="the probability of a success.") Double p,
                    @ParameterInfo(name="n", description="number of trials.") Integer n) {
        this.p = p;
        this.n = n;
        setup();
    }

    private void setup() { binomial = new BinomialDistribution(n, p); }

    @GeneratorInfo(name="Binomial", description="The binomial distribution of x successes in n trials given probability p of success of a single trial.")
    public Integer sample() {
        return binomial.sample();
    }

    public double density(Integer i) {
        return binomial.probability(i);
    }

    void setP(Double p) {
        if (!this.p.equals(p)) {
            this.p = p;
            setup();
        }
    }

    void setN(Integer n) {
        if (!this.n.equals(n)) {
            this.n = n;
            setup();
        }
    }

    Integer getN() { return n; }
    Double getP() { return p; }

    public String toString() {
        return getName();
    }
}
