package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.*;

public class GeneralNegativeBinomial extends ParametricDistribution<Integer>{

    private Value<Double> p;
    private Value<Double> r;
    private GammaDistribution gamma;
    private PoissonDistribution poisson;

    public GeneralNegativeBinomial(@ParameterInfo(name = rParamName, description = "the number of successes which can not be an integer.") Value<Double> r,
                                   @ParameterInfo(name = pParamName, description = "the probability of a success.") Value<Double> p) {
        super();
        this.p = p;
        this.r = r;

        constructDistribution(random);
    }

    public GeneralNegativeBinomial() {
        constructDistribution(random);
    }


    @Override
    protected void constructDistribution(RandomGenerator random) {
    }
    @GeneratorInfo(name = "GNB", verbClause = "has", narrativeName = "generalised negative binomial distribution",
            category = GeneratorCategory.PRIOR,
            description = "The generalised negative binomial distribution which parameter r can not be an integer.")
    @Override
    public RandomVariable<Integer> sample() {
        double alpha = r.value();
        double beta = (1 - p.value()) / p.value();
        gamma = new GammaDistribution(random, alpha, beta);
        double lamda = gamma.sample();
        poisson = new PoissonDistribution(random, lamda, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
        Integer result = poisson.sample();

        return new RandomVariable<>(null, result, this);
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(pParamName, p);
            put(nParamName, r);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case rParamName:
                this.r = value;
                break;
            case pParamName:
                this.p = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }


//        super.setParam(paramName, value); // constructDistribution
    }


    public String toString() {
        return getName();
    }
}
