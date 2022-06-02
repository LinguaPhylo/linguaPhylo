package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

import static lphy.core.distributions.DistributionConstants.pParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Bernoulli (coin toss) distribution prior.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Bernoulli extends PriorDistributionGenerator<Boolean> {
    private Value<Number> p;

    public Bernoulli(@ParameterInfo(name=pParamName, description="the probability of success.") Value<Number> p) {
        super();
        this.p = p;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) { }

    @GeneratorInfo(name="Bernoulli", verbClause = "has", narrativeName = "coin toss distribution prior",
            category = GeneratorCategory.PROB_DIST,
            examples = {"simpleBModelTest.lphy","simpleBModelTest2.lphy"},
            description="The coin toss distribution. With true (heads) having probability p.")
    public RandomVariable<Boolean> sample() {
        boolean success = (random.nextDouble() < doubleValue(p));
        return new RandomVariable<>("x", success, this);
    }

    public double density(Boolean success) {
        return success ? doubleValue(p) : (1.0 - doubleValue(p));
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(pParamName, p);
    }

    public void setP(Double p) {
        this.p.setValue(p);
    }

}
