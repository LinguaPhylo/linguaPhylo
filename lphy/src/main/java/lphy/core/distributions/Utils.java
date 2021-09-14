package lphy.core.distributions;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

public class Utils {

    private static RandomGenerator random = new MersenneTwister();

    public static RandomGenerator getRandom() {
        return random;
    }

    public static void setRandom(RandomGenerator r) {
        random = r;
    }

    public static double randomGamma(double shape, double scale) {
        return new GammaDistribution(shape, scale).sample();
    }
}
