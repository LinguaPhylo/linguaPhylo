package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.nParamName;

/**
 * Created by adru001 on 18/12/19.
 */
public class ExpMarkovChain implements GenerativeDistribution<Double[]> {

    private final static String initialMeanParamName = "initialMean";
    private final static String firstValueParamName = "firstValue";
    private Value<Double> initialMean;
    private Value<Double> firstValue;
    private Value<Integer> n;

    private RandomGenerator random;

    public ExpMarkovChain(@ParameterInfo(name = initialMeanParamName, narrativeName = "initial mean", description = "This is the mean of the exponential from which the first value of the chain is drawn.", optional = true) Value<Double> initialMean,
                          @ParameterInfo(name = firstValueParamName, description = "This is the value of the 1st element of the chain (X[0]).", optional = true) Value<Double> firstValue,
                          @ParameterInfo(name = nParamName, narrativeName = "number of steps", description = "the dimension of the return. Use either X[0] ~ Exp(mean=initialMean); or X[0] ~ LogNormal(meanlog, sdlog); Then X[i+1] ~ Exp(mean=X[i])") Value<Integer> n) {

        if ( (initialMean == null && firstValue == null) || (initialMean != null && firstValue != null) ) {
            throw new IllegalArgumentException("Require either " + initialMeanParamName + " or " + firstValueParamName);
        } else if (firstValue != null) {
            this.firstValue = firstValue;
        } else { // initialMean != null
            this.initialMean = initialMean;
        }

        this.n = n;
        this.random = Utils.getRandom();

    }

    @GeneratorInfo(name = "ExpMarkovChain",
            verbClause = "have",
            narrativeName = "smoothing prior in which each element has an exponential prior with a mean of the previous element in the chain",
            description = "A chain of random variables. X[0] ~ Exp(mean=initialMean) or X[0] ~ LogNormal(meanlog, sdlog); X[i+1] ~ Exp(mean=X[i])")
    public RandomVariable<Double[]> sample() {

        Double[] result = new Double[n.value()];
        ExponentialDistribution exp;
        if (firstValue != null) {
            // X[0] ~ Theta[0];
            result[0] = firstValue.value();
        } else {
            // X[0] ~ Exp(mean=initialMean);
            exp = new ExponentialDistribution(initialMean.value());
            result[0] = exp.sample();
        }
        // X[i] ~ Exp(mean=X[i-1])
        for (int i = 1; i < result.length; i++) {
            exp = new ExponentialDistribution(result[i-1]);
            result[i] = exp.sample();
        }
        return new RandomVariable<>("x", result, this);
    }

    public double logDensity(Double[] x) {

        double logDensity;
        ExponentialDistribution exp;
        if (firstValue != null) {
            logDensity = ((GenerativeDistribution1D) firstValue.getGenerator()).logDensity(x[0]);
        } else {
            exp = new ExponentialDistribution(initialMean.value());
            logDensity = exp.logDensity(x[0]);
        }
        // X[i] ~ Exp(mean=X[i-1])
        for (int i = 1; i < x.length; i++) {
            exp = new ExponentialDistribution(x[i-1]);
            logDensity += exp.logDensity(x[i]);
        }
        return logDensity;
    }

    public Map<String, Value> getParams() {
        if (firstValue != null) {
            return new TreeMap<>() {{
                put(firstValueParamName, firstValue);
                put(nParamName, n);
            }};
        } else {
            return new TreeMap<>() {{
                put(initialMeanParamName, initialMean);
                put(nParamName, n);
            }};
        }
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(initialMeanParamName)) initialMean = value;
        else if (paramName.equals(firstValueParamName)) firstValue = value;
        else if (paramName.equals(nParamName)) n = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }

    public Value<Double> getInitialMean() {
        return initialMean;
    }

    public Value<Double> getFirstValue() {
        return firstValue;
    }

    public Value<Integer> getN() {
        return n;
    }
}