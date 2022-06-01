package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

import static lphy.core.distributions.DistributionConstants.meanParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Exp implements GenerativeDistribution1D<Double> {

    private Value<Number> mean;

    private RandomGenerator random;

    ExponentialDistribution exp;

    public Exp(@ParameterInfo(name=meanParamName,
            description="the mean of an exponential distribution.") Value<Number> mean) {
        this.mean = mean;
        //this.rate = rate;
        //if (mean != null && rate != null) throw new IllegalArgumentException("Only one of mean and rate can be specified.");

        constructDistribution();
    }

    @GeneratorInfo(name="Exp", verbClause = "has", narrativeName = "exponential distribution prior",
            category = GeneratorCategory.PROB_DIST, examples = {"birthDeathRhoSampling.lphy","yuleRelaxed.lphy"},
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
    public void constructDistribution() {
        this.random = Utils.getRandom();
        exp = new ExponentialDistribution(random, doubleValue(mean));
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(meanParamName, mean);
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) {
            mean = value;
        } else
            throw new RuntimeException("Only valid parameter name is " + meanParamName);

        constructDistribution();
    }

    @Override
    public Double[] getDomainBounds() {
        throw new UnsupportedOperationException("TODO");
    }

    public double getMean() {
        if (mean != null) return doubleValue(mean);
        return 1.0;
    }

    public void setMean(double mean) {
        this.mean.setValue(mean);
        constructDistribution();
    }

    public String toString() {
        return getName();
    }
}
