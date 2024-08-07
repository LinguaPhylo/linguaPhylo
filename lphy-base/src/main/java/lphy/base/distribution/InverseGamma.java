package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.alphaParamName;
import static lphy.base.distribution.DistributionConstants.betaParamName;

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

    public InverseGamma(@ParameterInfo(name = alphaParamName, description = "the alpha parameter of inverse gamma.") Value<Number> alpha,
                        @ParameterInfo(name = DistributionConstants.betaParamName, description = "the beta parameter of inverse gamma.") Value<Number> beta) {
        super();
        this.alpha = alpha;
        this.beta = beta;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (alpha == null) throw new IllegalArgumentException("The " + alphaParamName + " value can't be null!");
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
            put(alphaParamName, alpha);
            put(DistributionConstants.betaParamName, beta);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(alphaParamName)) alpha = value;
        else if (paramName.equals(betaParamName)) beta = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
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