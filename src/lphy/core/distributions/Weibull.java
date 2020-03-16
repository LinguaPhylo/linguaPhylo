package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 18/12/19.
 */
public class Weibull implements GenerativeDistribution<Double> {

    private final String alphaParamName;
    private final String betaParamName;
    private Value<Double> alpha;
    private Value<Double> beta;

    public Weibull(@ParameterInfo(name="alpha", description="the first shape parameter of the Weibull distribution.") Value<Double> alpha,
                @ParameterInfo(name="beta", description="the second shape parameter of the Weibull distribution.") Value<Double> beta) {
        this.alpha = alpha;
        this.beta = beta;
        alphaParamName = getParamName(0);
        betaParamName = getParamName(1);
    }

    @GeneratorInfo(name="Weibull", description="The Weibull distribution.")
    public RandomVariable<Double> sample() {

        WeibullDistribution weibullDistribution = new WeibullDistribution(alpha.value(), beta.value());

        double randomVariable = weibullDistribution.sample();

        return new RandomVariable<>("x", randomVariable, this);
    }

    public double logDensity(Double d) {
        BetaDistribution betaDistribution = new BetaDistribution(alpha.value(), beta.value());
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
}