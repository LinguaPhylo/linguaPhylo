package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by adru001 on 18/12/19.
 */
public class Beta implements GenerativeDistribution1D<Double> {

    private final String alphaParamName;
    private final String betaParamName;
    private Value<Number> alpha;
    private Value<Number> beta;

    public Beta(@ParameterInfo(name="alpha", description="the first shape parameter.") Value<Number> alpha,
                @ParameterInfo(name="beta", description="the second shape parameter.") Value<Number> beta) {
        this.alpha = alpha;
        this.beta = beta;
        alphaParamName = getParamName(0);
        betaParamName = getParamName(1);
    }

    @GeneratorInfo(name="Beta", description="The beta probability distribution.")
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
    public Map<String,Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(alphaParamName, alpha);
        map.put(betaParamName, beta);
        return map;
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