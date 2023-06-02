package lphy.base.math;

import lphy.core.exception.LoggerUtils;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Random;

public class RandomUtils {

    public static final String SEED_PARA_NAME = "seed";
    public static final String SEED_PARA_DESC = "the seed value of the random number generator in LPhy";

    // for composing apache math distribution
    private static RandomGenerator random = new MersenneTwister();
    // for only using Java Random
    private static Random javaRandom = new Random();

    /**
     * @return a pseudo-random number generator developed by
     * Makoto Matsumoto and Takuji Nishimura during 1996-1997.
     * @see MersenneTwister
     */
    public static RandomGenerator getRandom() {
        return random;
    }

    /**
     * @return  a random number generator from java.util.random.
     * @see Random
     */
    public static Random getJavaRandom() {
        return javaRandom;
    }

    //TODO either create a new Random or setSeed
    @Deprecated
    public static void setRandom(RandomGenerator r) {
        random = r;
    }

    /**
     * Sets the seed of the underlying random number generator using an int seed.
     * Sequences of values generated starting with the same seeds should be identical.
     * @param seed  the seed value
     * @see RandomGenerator#setSeed(long)
     * @see Random#setSeed(long)
     */
    public static void setSeed(long seed) {
        random.setSeed(seed);
        javaRandom.setSeed(seed);
        LoggerUtils.log.info("Set seed " + seed + " to LPhy random number generator.");
    }


}
