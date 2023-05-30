package lphy.base.math;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

public class MathUtils {

    /**
     *
     * @param lambda the rate of the exponential distribution
     * @param random a random generator
     * @return a random variate from an exponential distribution with the given rate.
     */
    public static double nextExponential(double lambda, RandomGenerator random) {
        return Math.log(1-random.nextDouble())/(-lambda);
    }


    public static double randomGamma(double shape, double scale, RandomGenerator random) {
        return new GammaDistribution(random, shape, scale,
                GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
    }

}
