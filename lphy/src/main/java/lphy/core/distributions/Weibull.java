package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.WeibullDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.alphaParamName;
import static lphy.core.distributions.DistributionConstants.betaParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Weibull implements GenerativeDistribution<Double> {

    private Value<Number> alpha;
    private Value<Number> beta;

    WeibullDistribution weibullDistribution;

    public Weibull(@ParameterInfo(name = alphaParamName, description = "the first shape parameter of the Weibull distribution.") Value<Number> alpha,
                   @ParameterInfo(name = betaParamName, description = "the second shape parameter of the Weibull distribution.") Value<Number> beta) {
        this.alpha = alpha;
        this.beta = beta;

        constructDistribution();
    }

    @GeneratorInfo(name = "Weibull", description = "The Weibull distribution.")
    public RandomVariable<Double> sample() {

        double randomVariable = weibullDistribution.sample();

        return new RandomVariable<>("x", randomVariable, this);
    }

    public double logDensity(Double d) {
        return weibullDistribution.logDensity(d);
    }

    private void constructDistribution() {
        weibullDistribution = new WeibullDistribution(doubleValue(alpha), doubleValue(beta));
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
}