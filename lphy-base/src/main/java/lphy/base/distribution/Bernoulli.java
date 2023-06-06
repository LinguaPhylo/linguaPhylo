package lphy.base.distribution;

import lphy.core.model.GeneratorCategory;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.Map;

import static lphy.base.distribution.DistributionConstants.pParamName;

/**
 * Bernoulli (coin toss) distribution prior.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Bernoulli extends ParametricDistribution<Boolean> {
    private Value<Number> p;

    public Bernoulli(@ParameterInfo(name=pParamName, description="the probability of success.") Value<Number> p) {
        super();
        this.p = p;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) { }

    @GeneratorInfo(name="Bernoulli", verbClause = "has", narrativeName = "coin toss distribution prior",
            category = GeneratorCategory.PRIOR,
            examples = {"simpleBModelTest.lphy","simpleBModelTest2.lphy"},
            description="The coin toss distribution. With true (heads) having probability p.")
    public RandomVariable<Boolean> sample() {
        boolean success = (random.nextDouble() < ValueUtils.doubleValue(p));
        return new RandomVariable<>("x", success, this);
    }

    public double density(Boolean success) {
        return success ? ValueUtils.doubleValue(p) : (1.0 - ValueUtils.doubleValue(p));
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(pParamName, p);
    }

    public void setP(Double p) {
        this.p.setValue(p);
    }

}
