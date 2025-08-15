package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.Bool;
import org.phylospec.types.Probability;
import org.phylospec.types.impl.BoolImpl;

import java.util.Collections;
import java.util.Map;

import static lphy.base.distribution.DistributionConstants.pParamName;

/**
 * Bernoulli (coin toss) distribution prior.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Bernoulli extends ParametricDistribution<Bool> {
    private Value<Probability> p;

    public Bernoulli(@ParameterInfo(name=pParamName, description="the probability of success.")
                     Value<Probability> p) {
        super();
        this.p = p;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) { }

    @GeneratorInfo(name="Bernoulli", verbClause = "has", narrativeName = "coin toss distribution prior",
            category = GeneratorCategory.PRIOR,
            examples = {"simpleBModelTest.lphy","simpleBModelTest2.lphy"},
            description="The coin toss distribution. With true (heads) having probability p.")
    public RandomVariable<Bool> sample() {
        boolean success = (random.nextDouble() < p.value().getPrimitive());
        return new RandomVariable<>("x", new BoolImpl(success), this);
    }

    public double density(Bool success) {
        return success.getPrimitive() ? p.value().getPrimitive() : (1.0 - p.value().getPrimitive());
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(pParamName, p);
    }

    /**
     * Cannot use setters because of Number
     */

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(pParamName)) p = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }
}
