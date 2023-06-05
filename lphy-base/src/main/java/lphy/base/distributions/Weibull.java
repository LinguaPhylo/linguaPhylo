package lphy.base.distributions;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.components.GeneratorCategory;
import lphy.core.model.components.RandomVariable;
import lphy.core.model.components.Value;
import lphy.core.model.components.ValueUtils;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

/**
 * The Weibull distribution.
 * @author Alexei Drummond
 * @author Walter Xie
 * @see WeibullDistribution
 */
public class Weibull extends ParametricDistribution<Double> {

    private Value<Number> alpha;
    private Value<Number> beta;

    WeibullDistribution weibullDistribution;

    public Weibull(@ParameterInfo(name = DistributionConstants.alphaParamName, description = "the first shape parameter of the Weibull distribution.") Value<Number> alpha,
                   @ParameterInfo(name = DistributionConstants.betaParamName, description = "the second shape parameter of the Weibull distribution.") Value<Number> beta) {
        super();
        this.alpha = alpha;
        this.beta = beta;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        weibullDistribution = new WeibullDistribution(random, ValueUtils.doubleValue(alpha), ValueUtils.doubleValue(beta),
                WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
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
            put(DistributionConstants.alphaParamName, alpha);
            put(DistributionConstants.betaParamName, beta);
        }};
    }

}