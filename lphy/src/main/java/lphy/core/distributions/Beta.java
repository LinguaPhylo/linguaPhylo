package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.alphaParamName;
import static lphy.core.distributions.DistributionConstants.betaParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Beta implements GenerativeDistribution1D<Double> {

    private Value<Number> alpha;
    private Value<Number> beta;

    BetaDistribution betaDistribution;

    public Beta(@ParameterInfo(name = alphaParamName, description = "the first shape parameter.") Value<Number> alpha,
                @ParameterInfo(name = betaParamName, description = "the second shape parameter.") Value<Number> beta) {
        this.alpha = alpha;
        this.beta = beta;

        constructDistribution();
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
    public void constructDistribution() {
        // in case alpha/beta is type integer
        betaDistribution = new BetaDistribution(Utils.getRandom(), doubleValue(alpha), doubleValue(beta));
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(alphaParamName, alpha);
            put(betaParamName, beta);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(alphaParamName)) alpha = value;
        else if (paramName.equals(betaParamName)) beta = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        constructDistribution();
    }

    public String toString() {
        return getName();
    }

    private static final Double[] domainBounds = {0.0, 1.0};

    public Double[] getDomainBounds() {
        return domainBounds;
    }

}