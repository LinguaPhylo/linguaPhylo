package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.*;

/**
 * This uses the Pascal distribution actually,
 * where the input parameter r, the number of successes, is an integer.
 * @see org.apache.commons.math3.distribution.PascalDistribution
 * @author Walter Xie
 */
public class NegativeBinomial extends ParametricDistribution<Integer> implements GenerativeDistribution1D<Integer> {

    private Value<Double> p;
    private Value<Integer> r;

    org.apache.commons.math3.distribution.PascalDistribution pascalDist;

    public NegativeBinomial(@ParameterInfo(name = rParamName, description = "the number of successes.") Value<Integer> r,
                            @ParameterInfo(name = pParamName, description = "the probability of a success.") Value<Double> p) {
        super();
        this.p = p;
        this.r = r;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        pascalDist = new org.apache.commons.math3.distribution.PascalDistribution(random, r.value(), p.value());
    }

    @GeneratorInfo(name = "NegativeBinomial", narrativeName = "negative binomial distribution",
            description = "It uses the Pascal distribution with the given number of successes (integer) and probability of success.")
    public RandomVariable<Integer> sample() {
        return new RandomVariable<>(null, pascalDist.sample(), this);
    }

    public double density(Integer i) {
        return pascalDist.probability(i);
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(pParamName, p);
            put(nParamName, r);
        }};
    }

    private static final Integer[] domainBounds = {0, Integer.MAX_VALUE};
    public Integer[] getDomainBounds() {
        return domainBounds;
    }

}
