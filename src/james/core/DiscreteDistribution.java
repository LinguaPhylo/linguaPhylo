package james.core;

import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import java.util.Map;
import java.util.Random;

public class DiscreteDistribution implements GenerativeDistribution<Integer> {

    Value<double[]> probs;
    Random random;

    String probsParamName;


    public DiscreteDistribution(@ParameterInfo(name = "p", description = "the probability distribution over integer states.") Value<double[]> probs,
                Random random) {

        this.probs = probs;
        this.random = random;


    }
    public RandomVariable<Integer> sample() {

        double U = random.nextDouble();

        // TODO slow implementation! Should create cumulative probability distribution and use binary search!

        double[] p = probs.value();

        double sum = p[0];
        int i = 0;
        while (U > sum) {
            sum += p[i+1];
            i += 1;
        }
        return new RandomVariable<>("X", i, this);
    }

    @Override
    public Map<String, Value> getParams() {
        return null;
    }

    @Override
    public void setParam(String paramName, Value value) {

    }
}
