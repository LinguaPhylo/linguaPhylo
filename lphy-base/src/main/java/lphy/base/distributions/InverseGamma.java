package lphy.base.distributions;

import lphy.core.graphicalmodel.components.*;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

/**
 * Inverse-gamma distribution.
 * @see GammaDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class InverseGamma extends ParametricDistribution<Double> implements GenerativeDistribution1D<Double> {

    private Value<Number> alpha;
    private Value<Number> beta;

    GammaDistribution gammaDistribution;

    public InverseGamma(@ParameterInfo(name = DistributionConstants.alphaParamName, description = "the alpha parameter of inverse gamma.") Value<Number> alpha,
                        @ParameterInfo(name = DistributionConstants.betaParamName, description = "the beta parameter of inverse gamma.") Value<Number> beta) {
        super();
        this.alpha = alpha;
        this.beta = beta;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (alpha == null) throw new IllegalArgumentException("The " + DistributionConstants.alphaParamName + " value can't be null!");
        if (beta == null) throw new IllegalArgumentException("The " + DistributionConstants.betaParamName + " value can't be null!");

        double a = ValueUtils.doubleValue(alpha);
        double b = ValueUtils.doubleValue(beta);

        gammaDistribution = new GammaDistribution(random, a, 1.0/b, GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "InverseGamma",
            category = GeneratorCategory.PRIOR, examples = {"totalEvidence.lphy"},
            description = "The inverse-gamma probability distribution.")
    public RandomVariable<Double> sample() {
        // constructDistribution() only required in constructor and setParam
        double x = 1.0 / gammaDistribution.sample();
        return new RandomVariable<>(null, x, this);
    }

    @Override
    public double density(Double x) {
        return gammaDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.alphaParamName, alpha);
            put(DistributionConstants.betaParamName, beta);
        }};
    }

    public Value<Number> getBeta() {
        return beta;
    }

    public Value<Number> getAlpha() {
        return alpha;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}