package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

/**
 * Exponential distribution prior.
 * @see ExponentialDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Exp extends ParametricDistribution<Double> implements GenerativeDistribution1D<Double> {

    private Value<Number> mean;

    ExponentialDistribution exp;

    public Exp(@ParameterInfo(name= DistributionConstants.meanParamName,
            description="the mean of an exponential distribution.") Value<Number> mean) {
        super();
        this.mean = mean;
        //this.rate = rate;
        //if (mean != null && rate != null) throw new IllegalArgumentException("Only one of mean and rate can be specified.");

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        // use code available since apache math 3.1
        exp = new ExponentialDistribution(random, ValueUtils.doubleValue(mean), ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name="Exp", verbClause = "has", narrativeName = "exponential distribution prior",
            category = GeneratorCategory.PRIOR, examples = {"birthDeathRhoSampling.lphy","yuleRelaxed.lphy"},
            description="The exponential probability distribution.")
    public RandomVariable<Double> sample() {
        double x = - Math.log(random.nextDouble()) * getMean();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double aDouble) {
        return exp.logDensity(aDouble);
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(DistributionConstants.meanParamName, mean);
    }

    @Override
    public Double[] getDomainBounds() {
        throw new UnsupportedOperationException("TODO");
    }

    public double getMean() {
        if (mean != null) return ValueUtils.doubleValue(mean);
        return 1.0;
    }

    public void setMean(double mean) {
        this.mean.setValue(mean);
        constructDistribution(random);
    }

}
