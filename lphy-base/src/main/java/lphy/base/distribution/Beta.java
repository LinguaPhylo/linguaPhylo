package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution1D;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.PositiveReal;
import org.phylospec.types.Probability;
import org.phylospec.types.impl.ProbabilityImpl;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.alphaParamName;
import static lphy.base.distribution.DistributionConstants.betaParamName;

/**
 * Beta distribution prior.
 * @see BetaDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Beta extends ParametricDistribution<Probability> implements GenerativeDistribution1D<Probability, Double> {

    private Value<PositiveReal> alpha;
    private Value<PositiveReal> beta;

    BetaDistribution betaDistribution;

    public Beta(@ParameterInfo(name = alphaParamName, description = "the first shape parameter.")
                Value<PositiveReal> alpha,
                @ParameterInfo(name = betaParamName, description = "the second shape parameter.")
                Value<PositiveReal> beta) {
        super();
        this.alpha = alpha;
        this.beta = beta;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        // use code available since apache math 3.1
        betaDistribution = new BetaDistribution(random,
                ValueUtils.doublePrimitiveValue(alpha), ValueUtils.doublePrimitiveValue(beta),
                BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "Beta", verbClause = "has", narrativeName = "Beta distribution prior",
            category = GeneratorCategory.PRIOR,
            examples = {"birthDeathRhoSampling.lphy","simpleBModelTest.lphy"},
            description = "The beta probability distribution.")
    public RandomVariable<Probability> sample() {
        // constructDistribution() only required in constructor and setParam
        double randomVariable = betaDistribution.sample();

        Probability prob = new ProbabilityImpl(randomVariable);

        return new RandomVariable<>("x", prob, this);
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

    /**
     * Cannot use setters because of Number
     */

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(alphaParamName)) alpha = value;
        else if (paramName.equals(betaParamName)) beta = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }

    private static final Double[] domainBounds = {0.0, 1.0};

    public Double[] getDomainBounds() {
        return domainBounds;
    }

}