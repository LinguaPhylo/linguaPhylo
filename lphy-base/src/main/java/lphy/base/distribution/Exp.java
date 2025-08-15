package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.NonNegativeReal;
import org.phylospec.types.PositiveReal;
import org.phylospec.types.impl.NonNegativeRealImpl;

import java.util.Collections;
import java.util.Map;

import static lphy.base.distribution.DistributionConstants.meanParamName;

/**
 * Exponential distribution prior.
 * @see ExponentialDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Exp extends ParametricDistribution<NonNegativeReal> implements GenerativeDistribution1D<NonNegativeReal, Double> {

    private Value<PositiveReal> mean;

    ExponentialDistribution exp;

    public Exp(@ParameterInfo(name= meanParamName,
            description="the mean of an exponential distribution.") Value<PositiveReal> mean) {
        super();
        this.mean = mean;
        //this.rate = rate;
        //if (mean != null && rate != null) throw new IllegalArgumentException("Only one of mean and rate can be specified.");

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        // use code available since apache math 3.1
        exp = new ExponentialDistribution(random, mean.value().getPrimitive(),
                ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name="Exp", verbClause = "has", narrativeName = "exponential distribution prior",
            category = GeneratorCategory.PRIOR, examples = {"birthDeathRhoSampling.lphy","yuleRelaxed.lphy"},
            description="The exponential probability distribution.")
    public RandomVariable<NonNegativeReal> sample() {
        double x = - Math.log(random.nextDouble()) * getMean();
        // [0, Inf)
        NonNegativeReal nonNegativeReal = new NonNegativeRealImpl(x);
        return new RandomVariable<>("x", nonNegativeReal, this);
    }

    @Override
    public double density(NonNegativeReal aDouble) {
        return exp.logDensity(aDouble.getPrimitive());
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(meanParamName, mean);
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) mean = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }

    @Override
    public Double[] getDomainBounds() {
        return new Double[]{0.0, Double.POSITIVE_INFINITY};
    }

    public double getMean() {
        if (mean != null) return mean.value().getPrimitive();
        return 1.0;
    }

    //TODO this works strangely. When Value<Number> mean = 1, the mean.value is double 1.0
//    public void setMean(double mean) {
//        this.mean.setValue(mean);
//        constructDistribution(random);
//    }

}
