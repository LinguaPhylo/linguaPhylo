package lphy.core.distributions;

import beast.core.BEASTInterface;
import lphy.beast.BEASTContext;
import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

/**
 * Created by adru001 on 18/12/19.
 */
public class Poisson implements GenerativeDistribution<Integer> {

    private final String lambdaParamName;
    private Value<Double> lambda;

    private RandomGenerator random;

    public Poisson(@ParameterInfo(name="lambda", description="the expected number of events.") Value<Double> lambda) {
        this.lambda = lambda;
        this.random = Utils.getRandom();
        lambdaParamName = getParamName(0);
    }

    @GeneratorInfo(name="Poisson", description="The probability distribution of the number of events when the expected number of events is lambda, supported on the set { 0, 1, 2, 3, ... }.")
    public RandomVariable<Integer> sample() {

        PoissonDistribution poisson = new PoissonDistribution(lambda.value());
        return new RandomVariable<>("x", poisson.sample(), this);
    }

    public double density(Integer i) {
        PoissonDistribution poisson = new PoissonDistribution(lambda.value());
        return poisson.probability(i);
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(getParamName(0), lambda);
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(lambdaParamName)) {
            lambda = value;
        } else {
            throw new RuntimeException("Only valid parameter name is " + lambdaParamName);
        }
    }

    public void setLambda(double p) {
        this.lambda.setValue(p);
    }

    public String toString() {
        return getName();
    }

}
