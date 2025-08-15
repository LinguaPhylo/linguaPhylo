package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;
import org.phylospec.types.NonNegativeInt;
import org.phylospec.types.Probability;
import org.phylospec.types.impl.NonNegativeIntImpl;

import java.util.Collections;
import java.util.Map;

//TODO
public class Categorical extends ParametricDistribution<NonNegativeInt> {

    Value<Probability[]> probs;

    public Categorical(@ParameterInfo(name = DistributionConstants.pParamName,
            description = "the probability distribution over integer states 1 to K.")
                       Value<Probability[]> probs) {
        super();
        this.probs = probs;

    }

    @Override
    protected void constructDistribution(RandomGenerator random) { }

    @GeneratorInfo(name = "Categorical", verbClause = "has", narrativeName = "Categorical distribution prior",
            category = GeneratorCategory.PRIOR, description = "The categorical distribution.")
    public RandomVariable<NonNegativeInt> sample() {
        int i = sample(probs.value(), random);
        NonNegativeInt nonNegativeInt = new NonNegativeIntImpl(i);
        return new RandomVariable<>("X", nonNegativeInt, this);
    }

    /**
     * @param p       Probability[]
     * @param random  RandomGenerator
     * @return        i >= 0
     */
    public static int sample(Probability[] p, RandomGenerator random) {
        double U = random.nextDouble();

        // TODO slow implementation! Should create cumulative probability distribution and use binary search!
        double sum = p[0].getPrimitive();
        int i = 0;
        while (U > sum) {
            sum += p[i+1].getPrimitive();
            i += 1;
        }
        return i;
    }

    @Override
    public Map<String,Value> getParams() {
        return Collections.singletonMap(DistributionConstants.pParamName, probs);
    }

    public void setProbs(Value<Probability[]> probs) {
        this.probs = probs;
    }
}
