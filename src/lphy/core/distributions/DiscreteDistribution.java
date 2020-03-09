package lphy.core.distributions;

import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

public class DiscreteDistribution implements GenerativeDistribution<Integer> {

    Value<Double[]> probs;
    RandomGenerator random;

    String probsParamName;

    public DiscreteDistribution(@ParameterInfo(name = "p", description = "the probability distribution over integer states.") Value<Double[]> probs) {

        this.probs = probs;
        probsParamName = getParamName(0);
        this.random = Utils.getRandom();
    }
    public RandomVariable<Integer> sample() {

        int i = sample(probs.value(), random);
        return new RandomVariable<>("X", i, this);
    }

    public static int sample(Double[] p, RandomGenerator random) {
        double U = random.nextDouble();

        // TODO slow implementation! Should create cumulative probability distribution and use binary search!
        double sum = p[0];
        int i = 0;
        while (U > sum) {
            sum += p[i+1];
            i += 1;
        }
        return i;
    }

    @Override
    public Map<String, Value> getParams() {
        return null;
    }

    @Override
    public void setParam(String paramName, Value value) {

    }
}
