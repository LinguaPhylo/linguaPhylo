package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.graphicalModel.ValueUtils.doubleValue;
import static lphy.core.distributions.DistributionConstants.*;

/**
 * Created by adru001 on 18/12/19.
 */
public class Beta implements GenerativeDistribution1D<Double> {

    private Value<Number> alpha;
    private Value<Number> beta;

    public Beta(@ParameterInfo(name = alphaParamName, description = "the first shape parameter.") Value<Number> alpha,
                @ParameterInfo(name = betaParamName, description = "the second shape parameter.") Value<Number> beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    @GeneratorInfo(name = "Beta", description = "The beta probability distribution.")
    public RandomVariable<Double> sample() {

        BetaDistribution betaDistribution = new BetaDistribution(doubleValue(alpha), doubleValue(beta));

        double randomVariable = betaDistribution.sample();

        return new RandomVariable<>("x", randomVariable, this);
    }

    public double logDensity(Double d) {
        BetaDistribution betaDistribution = new BetaDistribution(doubleValue(alpha), doubleValue(beta));
        return betaDistribution.logDensity(d);
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
    }

    public String toString() {
        return getName();
    }

    private static final Double[] domainBounds = {0.0, 1.0};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}