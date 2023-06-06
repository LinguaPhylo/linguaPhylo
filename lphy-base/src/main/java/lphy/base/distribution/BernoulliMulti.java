package lphy.base.distribution;

import lphy.core.model.GeneratorCategory;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.IntegerValue;
import lphy.core.vectorization.IID;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.pParamName;

/**
 * A Bernoulli process of n trials.
 * Note: because there is a optional condition "minSuccesses",
 * It cannot be replaced by {@link IID}.
 *
 */
public class BernoulliMulti extends ParametricDistribution<Boolean[]> {
    private Value<Double> p;
    private Value<Integer> n;
    private Value<Integer> minSuccesses;

    public final String minSuccessesParamName = "minSuccesses";

    private final String repParamName = IID.REPLICATES_PARAM_NAME;
    private static final int MAX_TRIES = 1000;

    BinomialDistribution binomialDistribution;

    public BernoulliMulti(@ParameterInfo(name = pParamName, description = "the probability of success.") Value<Double> p,
                          @ParameterInfo(name = repParamName, description = "the number of bernoulli trials.") Value<Integer> n,
                          @ParameterInfo(name = minSuccessesParamName, description = "Optional condition: the minimum number of ones in the boolean array.", optional = true) Value<Integer> minSuccesses) {
        super();
        this.p = p;
        this.n = n;
        this.minSuccesses = minSuccesses;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        binomialDistribution = new BinomialDistribution(random, n.value(), p.value());
    }

    @GeneratorInfo(name = "Bernoulli", verbClause = "has", narrativeName = "coin toss distribution prior",
            category = GeneratorCategory.PRIOR,
            examples = {"simpleRandomLocalClock.lphy", "https://linguaphylo.github.io/tutorials/discrete-phylogeography/"},
            description = "The Bernoulli process for n iid trials. The success (true) probability is p. Produces a boolean n-tuple.")
    public RandomVariable<Boolean[]> sample() {

        Boolean[] b = bernoulli(p.value(), n.value());
        if (minSuccesses != null) {
            double[] p = new double[n.value()-minSuccesses.value()];

            double probSum = 0.0;
            int k = minSuccesses.value();
            for (int i = 0; i < p.length; i++) {
                p[i] = binomialDistribution.probability(i+k);
                probSum += p[i];
            }
            if (probSum > 0.0) {
                double U = random.nextDouble() * probSum;
                probSum = 0.0;
                int index = 0;
                for (int i = 0; i < p.length; i++) {
                    probSum += p[i];
                    if (probSum > U) {
                        index = i;
                        break;
                    }
                }
                RandomBooleanArray randomBooleanArray = new RandomBooleanArray(n, new IntegerValue(index+k, null));
                b = randomBooleanArray.sample().value();
            } else {
                throw new RuntimeException(minSuccessesParamName + " is too high and there is no probability above it due to numerical precision.");
            }
        }

        return new RandomVariable<>(null, b, this);
    }

    private int hammingWeight(Boolean[] b) {
        int sum = 0;
        for (Boolean i : b) {
            if (i) sum += 1;
        }
        return sum;
    }

    private Boolean[] bernoulli(double p, int n) {
        Boolean[] successes = new Boolean[n];
        for (int i = 0; i < successes.length; i++) {
            successes[i] = (random.nextDouble() < p);
        }
        return successes;
    }

    public double logDensity(Boolean[] successes) {
        double logP = 0.0;

        double lnp = Math.log(p.value());
        double ln1mp = Math.log(1.0 - p.value());

        for (int i = 0; i < successes.length; i++) {
            logP += successes[i] ? lnp : ln1mp;
        }
        return logP;
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(pParamName, p);
            put(repParamName, n);
            if (minSuccesses != null) put(minSuccessesParamName, minSuccesses);
        }};
    }

    public void setSuccessProbability(double p) {
        this.p.setValue(p);
    }

    public Value<Double> getP() {
        return getParams().get(pParamName);
    }

    public Value<Integer> getMinSuccesses() {
        return getParams().get(minSuccessesParamName);
    }
}
