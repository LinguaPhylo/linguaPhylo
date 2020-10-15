package lphy.math;

import org.apache.commons.math3.random.RandomGenerator;

public class MathUtils {

    /**
     *
     * @param lambda the rate of the exponential distribution
     * @param randomGenerator a random generator
     * @return a random variate from an exponential distribution with the given rate.
     */
    public static double nextExponential(double lambda, RandomGenerator randomGenerator) {
        return Math.log(1-randomGenerator.nextDouble())/(-lambda);
    }

}
