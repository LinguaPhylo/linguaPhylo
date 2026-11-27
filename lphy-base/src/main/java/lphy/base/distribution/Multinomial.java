package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

/**
 * The multinomial distribution: the counts of k events in n independent trials,
 * where event i has probability p[i]. The probabilities are normalised to sum to 1.
 */
public class Multinomial extends ParametricDistribution<Integer[]> {
    private Value<Integer> n;
    private Value<Double[]> p;

    public Multinomial(
            @ParameterInfo(name = DistributionConstants.nParamName, description = "number of trials.") Value<Integer> n,
            @ParameterInfo(name = DistributionConstants.pParamName, description = "event probabilities.") Value<Double[]> p) {
        super();
        this.n = n;
        this.p = p;

        constructDistribution(random);
    }

    public Multinomial(){
        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "Multinomial", verbClause = "has", narrativeName = "multinomial prior",
            category = GeneratorCategory.PRIOR, examples = {"gt16ReadCountModel.lphy"},
            description = "The multinomial probability distribution.")
    @Override
    public RandomVariable<Integer[]> sample() {
        return new RandomVariable<>(null, sample(random, n.value(), p.value()), this);
    }

    /**
     * Draws the counts with LPhy's random number generator, so that a seed makes them reproducible:
     * x[i] ~ Binomial(trials left, p[i] / probability left), and the last event takes the remaining trials.
     */
    static Integer[] sample(RandomGenerator random, int nTrials, Double[] prob) {
        if (nTrials < 0)
            throw new IllegalArgumentException("The number of trials must be non-negative, but n = " + nTrials);
        if (prob.length == 0)
            throw new IllegalArgumentException("Multinomial needs at least one event probability.");
        double total = 0;
        for (Double pi : prob) {
            if (pi == null || pi < 0 || Double.isNaN(pi))
                throw new IllegalArgumentException("Event probabilities must be non-negative, but p contains " + pi);
            total += pi;
        }
        if (total <= 0)
            throw new IllegalArgumentException("Event probabilities must not all be 0.");

        // probLeft[i] = p[i] + ... + p[k-1]
        double[] probLeft = new double[prob.length + 1];
        for (int i = prob.length - 1; i >= 0; i--)
            probLeft[i] = probLeft[i + 1] + prob[i];

        Integer[] counts = new Integer[prob.length];
        int trialsLeft = nTrials;
        for (int i = 0; i < prob.length - 1; i++) {
            if (trialsLeft == 0 || prob[i] == 0) {
                counts[i] = 0;
            } else if (probLeft[i + 1] == 0) {
                counts[i] = trialsLeft; // no later event can occur
            } else {
                // rounding can push the ratio slightly above 1
                double pi = Math.min(1.0, prob[i] / probLeft[i]);
                counts[i] = new BinomialDistribution(random, trialsLeft, pi).sample();
            }
            trialsLeft -= counts[i];
        }
        counts[prob.length - 1] = trialsLeft;
        return counts;
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.nParamName, n);
            put(DistributionConstants.pParamName, p);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case DistributionConstants.nParamName:
                this.n = value;
                break;
            case DistributionConstants.pParamName:
                this.p = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }
}
