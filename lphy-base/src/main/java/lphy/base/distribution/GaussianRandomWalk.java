package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.nParamName;
import static lphy.base.distribution.DistributionConstants.sdParamName;

/**
 * A smoothing prior in which each element has a normal prior centred on the
 * previous element of the chain. X[0] is either supplied via {@code firstValue}
 * (composable from another distribution) or drawn as Normal(initialMean, sd).
 * Then X[i+1] ~ Normal(X[i], sd).
 */
public class GaussianRandomWalk extends ParametricDistribution<Double[]> {

    public final static String initialMeanParamName = "initialMean";
    public final static String firstValueParamName = "firstValue";
    private Value<Double> initialMean;
    private Value<Double> firstValue;
    private Value<Double> sd;
    private Value<Integer> n;

    public GaussianRandomWalk(@ParameterInfo(name = initialMeanParamName, narrativeName = "initial mean",
            description = "The mean of the Normal from which the first value of the chain is drawn.",
            optional = true) Value<Double> initialMean,
                              @ParameterInfo(name = firstValueParamName,
                                      description = "The value of the 1st element of the chain (X[0]).",
                                      optional = true) Value<Double> firstValue,
                              @ParameterInfo(name = sdParamName, narrativeName = "step standard deviation",
                                      description = "The standard deviation of the Normal increment between consecutive elements.") Value<Double> sd,
                              @ParameterInfo(name = nParamName, narrativeName = "number of steps",
                                      description = "The length of the chain. Use either X[0] ~ Normal(mean=initialMean, sd=sd); " +
                                              "or supply X[0] via firstValue. Then X[i+1] ~ Normal(X[i], sd).") Value<Integer> n) {
        super();
        if ( (initialMean == null && firstValue == null) || (initialMean != null && firstValue != null) ) {
            throw new IllegalArgumentException("Require either " + initialMeanParamName + " or " + firstValueParamName);
        } else if (firstValue != null) {
            this.firstValue = firstValue;
        } else {
            this.initialMean = initialMean;
        }

        this.sd = sd;
        this.n = n;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) { }

    @GeneratorInfo(name = "GaussianRandomWalk", verbClause = "have",
            narrativeName = "smoothing prior in which each element has a normal prior centred on the previous element in the chain",
            category = GeneratorCategory.PRIOR,
            examples = {"structuredCoalescentSkyline.lphy"},
            description = "A chain of random variables. X[0] ~ Normal(mean=initialMean, sd=sd) " +
                    "or X[0] supplied via firstValue; X[i+1] ~ Normal(X[i], sd).")
    public RandomVariable<Double[]> sample() {

        Double[] result = new Double[n.value()];
        double sdVal = sd.value();
        NormalDistribution nd;
        if (firstValue != null) {
            result[0] = firstValue.value();
        } else {
            nd = new NormalDistribution(random, initialMean.value(), sdVal);
            result[0] = nd.sample();
        }
        for (int i = 1; i < result.length; i++) {
            nd = new NormalDistribution(random, result[i-1], sdVal);
            result[i] = nd.sample();
        }
        return new RandomVariable<>("x", result, this);
    }

    public double logDensity(Double[] x) {

        double sdVal = sd.value();
        double logDensity;
        NormalDistribution nd;
        if (firstValue != null) {
            logDensity = ((GenerativeDistribution1D) firstValue.getGenerator()).logDensity(x[0]);
        } else {
            nd = new NormalDistribution(random, initialMean.value(), sdVal);
            logDensity = nd.logDensity(x[0]);
        }
        for (int i = 1; i < x.length; i++) {
            nd = new NormalDistribution(random, x[i-1], sdVal);
            logDensity += nd.logDensity(x[i]);
        }
        return logDensity;
    }

    public Map<String, Value> getParams() {
        if (firstValue != null) {
            return new TreeMap<>() {{
                put(firstValueParamName, firstValue);
                put(sdParamName, sd);
                put(nParamName, n);
            }};
        } else {
            return new TreeMap<>() {{
                put(initialMeanParamName, initialMean);
                put(sdParamName, sd);
                put(nParamName, n);
            }};
        }
    }

    public void setInitialMean(double initialMean) {
        this.initialMean.setValue(initialMean);
        constructDistribution(random);
    }

    public void setFirstValue(double firstValue) {
        this.firstValue.setValue(firstValue);
        constructDistribution(random);
    }

    public void setSd(double sd) {
        this.sd.setValue(sd);
        constructDistribution(random);
    }

    public void setN(int n) {
        this.n.setValue(n);
        constructDistribution(random);
    }
}
