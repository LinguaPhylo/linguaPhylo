package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.NonNegativeInt;
import org.phylospec.types.Probability;
import org.phylospec.types.impl.NonNegativeIntImpl;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.*;

/**
 * This uses the Pascal distribution actually,
 * where the input parameter r, the number of successes, is an integer.
 * @see org.apache.commons.math3.distribution.PascalDistribution
 * @author Walter Xie
 */
public class NegativeBinomial extends ParametricDistribution<NonNegativeInt> implements GenerativeDistribution1D<NonNegativeInt, Integer> {

    private Value<Probability> p;
    private Value<NonNegativeInt> r;

    org.apache.commons.math3.distribution.PascalDistribution pascalDist;

    public NegativeBinomial(@ParameterInfo(name = rParamName, description = "the number of successes.")
                            Value<NonNegativeInt> r,
                            @ParameterInfo(name = pParamName, description = "the probability of a success.")
                            Value<Probability> p) {
        super();
        this.p = p;
        this.r = r;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        pascalDist = new org.apache.commons.math3.distribution.PascalDistribution(random,
                r.value().getPrimitive(), p.value().getPrimitive());
    }

    @GeneratorInfo(name = "NegativeBinomial", verbClause = "has", narrativeName = "negative binomial distribution",
            category = GeneratorCategory.PRIOR,
            description = "It uses the Pascal distribution with the given number of successes (integer) and probability of success.")
    public RandomVariable<NonNegativeInt> sample() {
        NonNegativeInt nonNegativeInt = new NonNegativeIntImpl(pascalDist.sample());
        return new RandomVariable<>(null, nonNegativeInt, this);
    }

    public double density(NonNegativeInt i) {
        return pascalDist.probability(i.getPrimitive());
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(pParamName, p);
            put(nParamName, r);
        }};
    }

    public void setP(Value<Probability> p) {
        this.p = p;
    }

    public void setR(Value<NonNegativeInt> r) {
        this.r = r;
    }

    private static final Integer[] domainBounds = {0, Integer.MAX_VALUE};
    public Integer[] getDomainBounds() {
        return domainBounds;
    }

}
