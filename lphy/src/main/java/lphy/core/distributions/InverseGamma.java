package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.alphaParamName;
import static lphy.core.distributions.DistributionConstants.betaParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Gamma distribution
 */
public class InverseGamma implements GenerativeDistribution1D<Double> {

    private Value<Number> alpha;
    private Value<Number> beta;

    GammaDistribution gammaDistribution;

    public InverseGamma(@ParameterInfo(name = alphaParamName, description = "the alpha parameter of inverse gamma.") Value<Number> alpha,
                        @ParameterInfo(name = betaParamName, description = "the beta parameter of inverse gamma.") Value<Number> beta) {

        this.alpha = alpha;
        if (alpha == null) throw new IllegalArgumentException("The " + alphaParamName + " value can't be null!");
        this.beta = beta;
        if (beta == null) throw new IllegalArgumentException("The " + betaParamName + " value can't be null!");

        constructDistribution();
    }

    @GeneratorInfo(name = "InverseGamma",
            category = GeneratorCategory.PROB_DIST, examples = {"totalEvidence.lphy"},
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
            put(alphaParamName, alpha);
            put(betaParamName, beta);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (alphaParamName.equals(paramName)) {
            alpha = value;
        } else if (betaParamName.equals(paramName)) {
            beta = value;
        } else {
            throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }

        constructDistribution();
    }

    @Override
    public void constructDistribution() {
        double a = doubleValue(alpha);
        double b = doubleValue(beta);

        gammaDistribution = new GammaDistribution(a, 1.0/b);
    }

    public String toString() {
        return getName();
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