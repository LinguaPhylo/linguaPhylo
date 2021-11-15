package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.*;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Weibull implements GenerativeDistribution<Double> {

    private Value<Double> alpha;
    private Value<Double> beta;

    public Weibull(@ParameterInfo(name = alphaParamName, description = "the first shape parameter of the Weibull distribution.") Value<Double> alpha,
                   @ParameterInfo(name = betaParamName, description = "the second shape parameter of the Weibull distribution.") Value<Double> beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    @GeneratorInfo(name = "Weibull", description = "The Weibull distribution.")
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
}