package lphy.base.distributions;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.components.GenerativeDistribution1D;
import lphy.core.model.components.RandomVariable;
import lphy.core.model.components.Value;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

/**
 * The binomial distribution of x successes in n trials given probability p of success of a single trial.
 * @see BinomialDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Binomial extends ParametricDistribution<Integer> implements GenerativeDistribution1D<Integer> {

    private Value<Double> p;
    private Value<Integer> n;

    BinomialDistribution binomial;

    public Binomial(@ParameterInfo(name = DistributionConstants.pParamName, description = "the probability of a success.") Value<Double> p,
                    @ParameterInfo(name = DistributionConstants.nParamName, description = "number of trials.") Value<Integer> n) {
        super();
        this.p = p;
        this.n = n;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        binomial = new BinomialDistribution(random, n.value(), p.value());
    }

    @GeneratorInfo(name = "Binomial", narrativeName = "binomial distribution",
            description = "The binomial distribution of x successes in n trials given probability p of success of a single trial.")
    public RandomVariable<Integer> sample() {
        return new RandomVariable<>(null, binomial.sample(), this);
    }

    public double density(Integer i) {
        return binomial.probability(i);
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.pParamName, p);
            put(DistributionConstants.nParamName, n);
        }};
    }

    private static final Integer[] domainBounds = {0, Integer.MAX_VALUE};
    public Integer[] getDomainBounds() {
        return domainBounds;
    }

}
