package lphy.core.distributions;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.Exponential;
import lphy.beast.BEASTContext;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class Exp implements GenerativeDistribution<Double> {

    private final String meanParamName;
    private Value<Double> mean;
    //private final String rateParamName;
    //private Value<Double> rate;

    private RandomGenerator random;

    public Exp(@ParameterInfo(name="mean", description="the mean of an exponential distribution.") Value<Double> mean) {
        this.mean = mean;
        //this.rate = rate;

        //if (mean != null && rate != null) throw new IllegalArgumentException("Only one of mean and rate can be specified.");

        this.random = Utils.getRandom();
        meanParamName = getParamName(0);
        //rateParamName = getParamName(1);
    }

    @GeneratorInfo(name="Exp", description="The exponential probability distribution.")
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
        return Collections.singletonMap(getParamName(0), mean);
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
        //if (rate != null) return 1.0/rate.value();
        return 1.0;
    }

    public void setMean(double mean) {
        this.mean.setValue(mean);
    }

    public String toString() {
        return getName();
    }

    public BEASTInterface toBEAST(BEASTInterface value, Map beastObjects) {
        Exponential exponential = new Exponential();
        exponential.setInputValue("mean", beastObjects.get(getParams().get("mean")));
        exponential.initAndValidate();
        return BEASTContext.createPrior(exponential, (RealParameter)value);
    }
}
