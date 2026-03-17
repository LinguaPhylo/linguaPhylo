package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.*;

/**
 * Poisson indicator prior matching MASCOT's GLM indicator prior exactly.
 * <p>
 * Samples k ~ Poisson(λ) | k ≤ n, then places k ones uniformly at random
 * among n positions.
 * <p>
 * The joint probability of an indicator vector x of length n with k = sum(x) ones is:
 * <pre>
 *   P(x | n, λ) = Poisson(k; λ) / Z(n, λ) / C(n, k)
 * </pre>
 * where Z(n, λ) = Σ_{j=0}^{n} Poisson(j; λ) is the truncation normaliser
 * and C(n, k) = n! / (k! (n-k)!) accounts for the uniform placement.
 *
 * @author Toby (requested), Claude (implementation)
 */
public class PoissonIndicators implements GenerativeDistribution<Boolean[]> {

    public static final String nParamName = "n";
    public static final String lambdaParamName = "lambda";

    private Value<Integer> n;
    private Value<Number> lambda;

    private Random random;

    static final int MAX_TRIES = 10000;

    public PoissonIndicators(
            @ParameterInfo(name = nParamName,
                    description = "the number of indicators (length of the boolean array).")
            Value<Integer> n,
            @ParameterInfo(name = lambdaParamName,
                    description = "the expected number of active indicators (Poisson rate parameter).")
            Value<Number> lambda) {
        this.n = n;
        this.lambda = lambda;
        this.random = RandomUtils.getJavaRandom();
    }

    @GeneratorInfo(name = "PoissonIndicators",
            category = GeneratorCategory.PRIOR,
            description = "Poisson indicator prior matching MASCOT's GLM indicator prior. " +
                    "Draws k ~ Poisson(λ) conditioned on k ≤ n, then places k ones " +
                    "uniformly at random among n positions.")
    public RandomVariable<Boolean[]> sample() {
        int nVal = n.value();
        double lambdaVal = ValueUtils.doubleValue(lambda);

        PoissonDistribution poisson = new PoissonDistribution(
                RandomUtils.getRandom(), lambdaVal,
                PoissonDistribution.DEFAULT_EPSILON,
                PoissonDistribution.DEFAULT_MAX_ITERATIONS);

        // Sample k ~ Poisson(λ) | k ≤ n
        int k = -1;
        int count = 0;
        while (k < 0 || k > nVal) {
            k = poisson.sample();
            count++;
            if (count > MAX_TRIES) {
                throw new RuntimeException(
                        "Failed to draw Poisson(λ=" + lambdaVal + ") ≤ " + nVal +
                                " after " + MAX_TRIES + " attempts.");
            }
        }

        // Place k ones uniformly at random among n positions
        List<Boolean> indicators = new ArrayList<>(nVal);
        for (int i = 0; i < k; i++) {
            indicators.add(true);
        }
        while (indicators.size() < nVal) {
            indicators.add(false);
        }
        Collections.shuffle(indicators, random);

        return new RandomVariable<>("x", indicators.toArray(new Boolean[0]), this);
    }

    public double logDensity(Boolean[] x) {
        int nVal = n.value();
        double lambdaVal = ValueUtils.doubleValue(lambda);

        if (x.length != nVal) {
            return Double.NEGATIVE_INFINITY;
        }

        int k = 0;
        for (boolean b : x) {
            if (b) k++;
        }

        PoissonDistribution poisson = new PoissonDistribution(lambdaVal);

        // log Poisson(k; λ)
        double logPoissonK = poisson.logProbability(k);

        // log Z(n, λ) = log Σ_{j=0}^{n} Poisson(j; λ)
        double logZ = logTruncationNormaliser(poisson, nVal);

        // log C(n, k)
        double logBinom = logBinomial(nVal, k);

        return logPoissonK - logZ - logBinom;
    }

    private static double logTruncationNormaliser(PoissonDistribution poisson, int n) {
        // Compute log( Σ_{j=0}^{n} Poisson(j; λ) )
        // Use log-sum-exp for numerical stability
        double[] logProbs = new double[n + 1];
        double maxLogProb = Double.NEGATIVE_INFINITY;
        for (int j = 0; j <= n; j++) {
            logProbs[j] = poisson.logProbability(j);
            if (logProbs[j] > maxLogProb) {
                maxLogProb = logProbs[j];
            }
        }
        double sumExp = 0.0;
        for (int j = 0; j <= n; j++) {
            sumExp += Math.exp(logProbs[j] - maxLogProb);
        }
        return maxLogProb + Math.log(sumExp);
    }

    private static double logBinomial(int n, int k) {
        // log C(n, k) = log(n!) - log(k!) - log((n-k)!)
        double result = 0.0;
        for (int i = 1; i <= k; i++) {
            result += Math.log(n - k + i) - Math.log(i);
        }
        return result;
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(nParamName, n);
            put(lambdaParamName, lambda);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(nParamName)) n = value;
        else if (paramName.equals(lambdaParamName)) lambda = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public Value<Integer> getN() {
        return n;
    }

    public Value<Number> getLambda() {
        return lambda;
    }
}
