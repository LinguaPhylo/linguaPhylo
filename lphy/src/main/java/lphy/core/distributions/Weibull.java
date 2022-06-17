package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.alphaParamName;
import static lphy.core.distributions.DistributionConstants.betaParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;
import static org.apache.commons.math3.distribution.WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY;

/**
 * The Weibull distribution.
 * @author Alexei Drummond
 * @author Walter Xie
 * @see WeibullDistribution
 */
public class Weibull extends PriorDistributionGenerator<Double> {

    private Value<Number> alpha;
    private Value<Number> beta;

    WeibullDistribution weibullDistribution;

    public Weibull(@ParameterInfo(name = alphaParamName, description = "the first shape parameter of the Weibull distribution.") Value<Number> alpha,
                   @ParameterInfo(name = betaParamName, description = "the second shape parameter of the Weibull distribution.") Value<Number> beta) {
        super();
        this.alpha = alpha;
        this.beta = beta;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        weibullDistribution = new WeibullDistribution(random, doubleValue(alpha), doubleValue(beta),
                DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "Weibull", category = GeneratorCategory.PRIOR,
            description = "The Weibull distribution.")
    public RandomVariable<Double> sample() {

        double randomVariable = weibullDistribution.sample();

        return new RandomVariable<>("x", randomVariable, this);
    }

    public double logDensity(Double d) {
        return weibullDistribution.logDensity(d);
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(alphaParamName, alpha);
            put(betaParamName, beta);
        }};
    }

}