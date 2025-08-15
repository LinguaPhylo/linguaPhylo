package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.NonNegativeInt;
import org.phylospec.types.Probability;
import org.phylospec.types.impl.NonNegativeIntImpl;

import java.util.Map;
import java.util.TreeMap;

/**
 * The binomial distribution of x successes in n trials given probability p of success of a single trial.
 * @see BinomialDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Binomial extends ParametricDistribution<NonNegativeInt> implements GenerativeDistribution1D<NonNegativeInt,Integer> {

    private Value<Probability> p;
    private Value<NonNegativeInt> n;

    BinomialDistribution binomial;

    public Binomial(@ParameterInfo(name = DistributionConstants.pParamName, description = "the probability of a success.")
                    Value<Probability> p,
                    @ParameterInfo(name = DistributionConstants.nParamName, description = "number of trials.")
                    Value<NonNegativeInt> n) {
        super();
        this.p = p;
        this.n = n;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        binomial = new BinomialDistribution(random, n.value().getPrimitive(), p.value().getPrimitive());
    }

    @GeneratorInfo(name = "Binomial", narrativeName = "binomial distribution",
            description = "The binomial distribution of x successes in n trials given probability p of success of a single trial.")
    public RandomVariable<NonNegativeInt> sample() {
        NonNegativeInt nonNegativeInt = new NonNegativeIntImpl(binomial.sample());
        return new RandomVariable<>(null, nonNegativeInt, this);
    }

    public double density(NonNegativeInt i) {
        return binomial.probability(i.getPrimitive());
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.pParamName, p);
            put(DistributionConstants.nParamName, n);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(DistributionConstants.pParamName)) p = value;
        else if (paramName.equals(DistributionConstants.nParamName)) n = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }

    public void setP(Value<Probability> p) {
        this.p = p;
    }

    public void setN(Value<NonNegativeInt> n) {
        this.n = n;
    }

    private static final Integer[] domainBounds = {0, Integer.MAX_VALUE};
    public Integer[] getDomainBounds() {
        return domainBounds;
    }

}
