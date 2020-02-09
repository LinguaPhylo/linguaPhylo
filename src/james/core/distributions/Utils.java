package james.core.distributions;

import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Random;

public class Utils {

    private static Random random = new Random();

    public static Random getRandom() {
        return random;
    }

    public static void setRandom(Random r) {
        random = r;
    }

    public static double randomGamma(double shape, double scale) {
        return new GammaDistribution(shape, scale).sample();
    }
}
