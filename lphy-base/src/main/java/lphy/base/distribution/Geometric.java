package lphy.base.distribution;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.GenerativeDistribution1D;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.model.component.RandomVariable;
import lphy.core.model.component.Value;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

/**
 * The discrete probability distribution of the number of failures
 * before the first success given a fixed probability of success p.
 * @see GeometricDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Geometric extends ParametricDistribution<Integer> implements GenerativeDistribution1D<Integer> {

    private Value<Double> p;

    GeometricDistribution geom;

    public Geometric(@ParameterInfo(name= DistributionConstants.pParamName, description="the probability of success.") Value<Double> p) {
        super();
        this.p = p;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        geom = new GeometricDistribution(random, p.value());
    }

    @GeneratorInfo(name="Geometric", category = GeneratorCategory.PRIOR,
            description="The probability distribution of the number of failures before the first success given a fixed probability of success p, supported on the set { 0, 1, 2, 3, ... }.")
    public RandomVariable<Integer> sample() {
       return new RandomVariable<>(null, geom.sample(), this);
    }

    public double density(Integer i) {
        return geom.probability(i);
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(DistributionConstants.pParamName, p);
    }

    public void setSuccessProbability(double p) {
        this.p.setValue(p);
    }

    private static final Integer[] domainBounds = {0, Integer.MAX_VALUE};
    public Integer[] getDomainBounds() {
        return domainBounds;
    }
}
