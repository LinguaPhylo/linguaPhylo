package lphy.base.distribution;

import lphy.base.math.MathUtils;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.concParamName;
import static lphy.base.distribution.DistributionConstants.meanParamName;

/**
 * The Weighted Dirichlet distribution, denoted WeightedDirichlet(α, w, μ), is
 * a transformation of the standard Dirichlet distribution that produces rate variables 
 * with a specified weighted mean (default 1).
 * <br>
 * R code construction:
 * # Sample
 * rwd <- function(alpha, w, mu) {
 *   k <- length(alpha)
 *   W <- sum(w)
 *   x <- rgamma(k, alpha) 
 *   x <- x / sum(x) # x are Dirichlet distributed
 *   # normalise so that weighted mean is mu
 *   r <- mu * W * x / w 
 *   return (r) 
 * }
 * @see Dirichlet
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class WeightedDirichlet extends ParametricDistribution<Double[]> {

    public static final String weightsParamName = "weights";

    private Value<Number[]> concentration;
    private Value<Integer[]> weights;
    private Value<Number> mean;

    private final double DEFAULT_MEAN = 1.0;

    public WeightedDirichlet(@ParameterInfo(name = concParamName, narrativeName = "concentration", description = "the concentration parameters of the scaled Dirichlet distribution.") Value<Number[]> concentration,
                             @ParameterInfo(name = weightsParamName, description = "the relative weight parameters of the scaled Dirichlet distribution.") Value<Integer[]> weights,
                             @ParameterInfo(name = meanParamName, optional = true,
                                     description = "the expected weighted mean of the values, default to 1.") Value<Number> mean) {
        super();
        this.concentration = concentration;
        this.weights = weights;
        this.mean = mean;
        Number[] conc = concentration.value();
        Integer[] weight = weights.value();
        if (conc ==null || weight == null || conc.length != weight.length)
            throw new IllegalArgumentException("The concentration parameters must have the same length of weight parameters !");
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {  }

    @GeneratorInfo(name = "WeightedDirichlet",
            category = GeneratorCategory.PRIOR, examples = {"totalEvidence.lphy","weightedDirichlet.lphy"},
            description = "The scaled dirichlet probability distribution. " +
                    "The weighted mean of values must equal to the expected weighted mean (default to 1).")
    public RandomVariable<Double[]> sample() {

        // step 1: Sample from standard Dirichlet with expected mean to 1
        Dirichlet dirichlet = new Dirichlet(concentration, null);
        RandomVariable<Double[]> xRV = dirichlet.sample();
        Double[] x = xRV.value();

        // step 2: Transform to weighted rates
        //         and adjust to the given expected mean if it is not 1:
        Number[] w = weights.value();
        // the expected mean default to 1
        double expectedMean = DEFAULT_MEAN;
        if (mean != null)
            expectedMean = mean.value().doubleValue();
        int dim = w.length;
        double weightSum = MathUtils.sumArray(w);
        for (int i = 0; i < dim; i++) {
            x[i] = expectedMean * x[i] * weightSum / w[i].doubleValue();
        }

        // step 3: validation, the weighted mean = sum(x[i] * weight[i]) / sum(weight[i])
        double weightedSumX = 0.0;
        for (int i = 0; i < x.length; i++) {
            double v = x[i] * w[i].doubleValue();
            weightedSumX += v;
        }
        double weightedMeanX = weightedSumX / weightSum;
        if (Math.abs(weightedMeanX - expectedMean) > 1e-6)
            throw new RuntimeException("The weighted mean of values (" + weightedMeanX +
                    ") differs significantly from the expected mean of values (" + expectedMean +") !");

        return new RandomVariable<>(null, x, this);
    }

    public double density(Double[] x) {
        // TODO
        throw new UnsupportedOperationException("WeightedDirichlet density is not supported yet");
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(concParamName, concentration);
            put(weightsParamName, weights);
            if (mean != null) put(meanParamName, mean);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(concParamName)) concentration = value;
        else if (paramName.equals(weightsParamName)) weights = value;
        else if (paramName.equals(meanParamName)) mean = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }
    public Value<Number[]> getConcentration() {
        return concentration;
    }

    public Value<Integer[]> getWeights() {
        return weights;
    }

    public Value<Number> getMean() {
        if (mean != null)
            return mean;
        return new Value<>("mean", DEFAULT_MEAN);
    }
}
