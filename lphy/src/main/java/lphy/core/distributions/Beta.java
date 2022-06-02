package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.alphaParamName;
import static lphy.core.distributions.DistributionConstants.betaParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Beta distribution prior.
 * @see BetaDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Beta extends PriorDistributionGenerator<Double> implements GenerativeDistribution1D<Double> {

    private Value<Number> alpha;
    private Value<Number> beta;

    BetaDistribution betaDistribution;

    public Beta(@ParameterInfo(name = alphaParamName, description = "the first shape parameter.") Value<Number> alpha,
                @ParameterInfo(name = betaParamName, description = "the second shape parameter.") Value<Number> beta) {
        super();
        this.alpha = alpha;
        this.beta = beta;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        // in case alpha/beta is type integer
        betaDistribution = new BetaDistribution(random, doubleValue(alpha), doubleValue(beta));
    }

    @GeneratorInfo(name = "Beta", verbClause = "has", narrativeName = "Beta distribution prior",
            category = GeneratorCategory.PROB_DIST,
            examples = {"birthDeathRhoSampling.lphy","simpleBModelTest.lphy"},
            description = "The beta probability distribution.")
    public RandomVariable<Double> sample() {
        // constructDistribution() only required in constructor and setParam
        double randomVariable = betaDistribution.sample();

        return new RandomVariable<>("x", randomVariable, this);
    }

    public double logDensity(Double d) {
        return betaDistribution.logDensity(d);
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(alphaParamName, alpha);
            put(betaParamName, beta);
        }};
    }

    private static final Double[] domainBounds = {0.0, 1.0};

    public Double[] getDomainBounds() {
        return domainBounds;
    }

}