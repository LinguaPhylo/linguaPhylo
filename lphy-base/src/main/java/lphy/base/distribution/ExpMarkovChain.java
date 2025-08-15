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
import org.phylospec.types.PositiveInt;
import org.phylospec.types.PositiveReal;
import org.phylospec.types.impl.NonNegativeRealImpl;
import org.phylospec.types.impl.PositiveIntImpl;
import org.phylospec.types.impl.PositiveRealImpl;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.nParamName;

/**
 * A smoothing prior in which each element has an exponential prior with a mean of
 * the previous element in the chain.
 */
public class ExpMarkovChain extends ParametricDistribution<NonNegativeReal[]> {

    public final static String initialMeanParamName = "initialMean";
    public final static String firstValueParamName = "firstValue";
    private Value<PositiveReal> initialMean;
    private Value<NonNegativeReal> firstValue;
    private Value<PositiveInt> n;

    public ExpMarkovChain(@ParameterInfo(name = initialMeanParamName, narrativeName = "initial mean",
            description = "This is the mean of the exponential from which the first value of the chain is drawn.",
            optional = true) Value<PositiveReal> initialMean,
                          @ParameterInfo(name = firstValueParamName,
                                  description = "This is the value of the 1st element of the chain (X[0]).",
                                  optional = true) Value<NonNegativeReal> firstValue,
                          @ParameterInfo(name = nParamName, narrativeName = "number of steps",
                                  description = "the dimension of the return. Use either X[0] ~ Exp(mean=initialMean); " +
                                          "or X[0] ~ LogNormal(meanlog, sdlog); Then X[i+1] ~ Exp(mean=X[i])")
                          Value<PositiveInt> n) {
        super();
        if ( (initialMean == null && firstValue == null) || (initialMean != null && firstValue != null) ) {
            throw new IllegalArgumentException("Require either " + initialMeanParamName + " or " + firstValueParamName);
        } else if (firstValue != null) {
            this.firstValue = firstValue;
        } else { // initialMean != null
            this.initialMean = initialMean;
        }

        this.n = n;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) { }

    @GeneratorInfo(name = "ExpMarkovChain", verbClause = "have",
            narrativeName = "smoothing prior in which each element has an exponential prior with a mean of the previous element in the chain",
            category = GeneratorCategory.PRIOR,
            examples = {"skylineCoalescent.lphy", "https://linguaphylo.github.io/tutorials/skyline-plots/"},
            description = "A chain of random variables. X[0] ~ Exp(mean=initialMean) or X[0] ~ LogNormal(meanlog, sdlog); X[i+1] ~ Exp(mean=X[i])")
    public RandomVariable<NonNegativeReal[]> sample() {

        NonNegativeReal[] result = new NonNegativeReal[n.value().getPrimitive()];
        ExponentialDistribution exp;
        if (firstValue != null) {
            // X[0] ~ Theta[0];
            result[0] = firstValue.value();
        } else {
            // X[0] ~ Exp(mean=initialMean);
            exp = new ExponentialDistribution(random, initialMean.value().getPrimitive(),
                    ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
            NonNegativeReal nonNegativeReal = new NonNegativeRealImpl(exp.sample());
            result[0] = nonNegativeReal;
        }
        // X[i] ~ Exp(mean=X[i-1])
        for (int i = 1; i < result.length; i++) {
            exp = new ExponentialDistribution(random, result[i-1].getPrimitive(),
                    ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
            NonNegativeReal nonNegativeReal = new NonNegativeRealImpl(exp.sample());
            result[i] = nonNegativeReal;
        }
        return new RandomVariable<>("x", result, this);
    }

    public double logDensity(NonNegativeReal[] x) {

        double logDensity;
        ExponentialDistribution exp;
        if (firstValue != null) {
            logDensity = ((GenerativeDistribution1D) firstValue.getGenerator()).logDensity(x[0]);
        } else {
            exp = new ExponentialDistribution(random, initialMean.value().getPrimitive(),
                    ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
            logDensity = exp.logDensity(x[0].getPrimitive());
        }
        // X[i] ~ Exp(mean=X[i-1])
        for (int i = 1; i < x.length; i++) {
            exp = new ExponentialDistribution(random, x[i-1].getPrimitive(),
                    ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
            logDensity += exp.logDensity(x[i].getPrimitive());
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

    //TODO cannot work with Number. Perhaps change to setParam
    public void setInitialMean(double initialMean) {
        PositiveReal positiveReal = new PositiveRealImpl(initialMean);
        this.initialMean.setValue(positiveReal);
        constructDistribution(random);
    }

    public void setFirstValue(double firstValue) {
        NonNegativeReal nonNegativeReal = new NonNegativeRealImpl(firstValue);
        this.firstValue.setValue(nonNegativeReal);
        constructDistribution(random);
    }

    public void setN(int n) {
        PositiveInt positiveInt = new PositiveIntImpl(n);
        this.n.setValue(positiveInt);
        constructDistribution(random);
    }

}