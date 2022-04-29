package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

import static lphy.core.distributions.DistributionConstants.meanParamName;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Exp implements GenerativeDistribution<Double> {

    private Value<Double> mean;

    private RandomGenerator random;

    public Exp(@ParameterInfo(name=meanParamName,
            description="the mean of an exponential distribution.") Value<Double> mean) {
        this.mean = mean;
        //this.rate = rate;

        //if (mean != null && rate != null) throw new IllegalArgumentException("Only one of mean and rate can be specified.");

        this.random = Utils.getRandom();
    }

    @GeneratorInfo(name="Exp", verbClause = "has", narrativeName = "exponential distribution prior",
            category = GeneratorCategory.PRIOR, examples = {"yuleRelaxed.lphy"},
            description="The exponential probability distribution.")
    public RandomVariable<Double> sample() {

        double x = - Math.log(random.nextDouble()) * getMean();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double aDouble) {
        return 0;
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(meanParamName, mean);
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(meanParamName)) {
            mean = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + meanParamName);
        }
    }

    public double getMean() {
        if (mean != null) return mean.value();
        return 1.0;
    }

    public void setMean(double mean) {
        this.mean.setValue(mean);
    }

    public String toString() {
        return getName();
    }
}
