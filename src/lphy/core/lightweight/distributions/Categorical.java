package lphy.core.lightweight.distributions;

import lphy.core.distributions.Utils;
import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.graphicalModel.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

public class Categorical implements LightweightGenerativeDistribution<Integer> {

    Double[] probs;
    RandomGenerator random;

    public Categorical(@ParameterInfo(name = "p", description = "the probability distribution over integer states 1 to K.") Double[] probs) {

        this.probs = probs;
        this.random = Utils.getRandom();
    }
    public Integer sample() {
        return sample(probs, random);
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

    Double[] getProbs() {
        return probs;
    }

    void setProbs(Double[] probs) {
        this.probs = probs;
    }
}
