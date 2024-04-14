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


        /**
         * Count the number of ways to select two out of n elements.
         *
         * @param n total number of elements
         * @return The number of ways to select two from n elements
         */
        public static double choose2(final int n) {
            return n * (n - 1) / 2.0;
        }


}
