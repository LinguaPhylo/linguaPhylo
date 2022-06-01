package lphy.core.distributions;

import lphy.util.LoggerUtils;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

public class Utils {

    public static final String SEED_PARA_NAME = "seed";
    public static final String SEED_PARA_DESC = "the seed value of the random number generator in LPhy";

    private static RandomGenerator random = new MersenneTwister();

    /**
     * @return a pseudo-random number generator developed by
     * Makoto Matsumoto and Takuji Nishimura during 1996-1997.
     * @see MersenneTwister
     */
    public static RandomGenerator getRandom() {
        return random;
    }

    public static void setRandom(RandomGenerator r) {
        random = r;
    }

    public static double randomGamma(double shape, double scale) {
        return new GammaDistribution(getRandom(), shape, scale).sample();
    }

    /**
     * Sets the seed of the underlying random number generator using an int seed.
     * Sequences of values generated starting with the same seeds should be identical.
     * @param seed  the seed value
     * @see RandomGenerator#setSeed(int)
     */
    public static void setSeed(int seed) {
        random.setSeed(seed);
        LoggerUtils.log.info("Set seed " + seed + " to LPhy random number generator.");
    }


}
