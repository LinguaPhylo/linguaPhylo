package lphy.base.math;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;

public class MathUtils {

    public static DecimalFormat defaultDecimalFormat = new DecimalFormat();

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

    /**
     * @param d        real number
     * @param sigFigs  significant figures
     * @return         round double based on significant digits automatically
     */
    public static String formatScientific(double d, int sigFigs) {
        DecimalFormat df;
        double positiveD = Math.abs(d);
        if (positiveD > 1000 || positiveD < 0.001) {
            BigDecimal bd = new BigDecimal(d);
            bd = bd.round(new MathContext(sigFigs));
            double rounded = bd.doubleValue();
            df = new DecimalFormat("0." + "0".repeat(sigFigs - 1) + "E0");
            return df.format(rounded);
        }

        df = defaultDecimalFormat;
        return df.format(d);
    }

    public static String formatScientific(int integer, int sigFigs) {
        return defaultDecimalFormat.format(integer);
    }

    /*
    The fastest way to sum all elements in a Java array is generally to use a basic for loop.
     */

    public static int sumArray(int[] arr) {
        int sum = 0;
        for (int e : arr)
            sum += e;
        return sum;
    }

    public static double sumArray(double[] arr) {
        double sum = 0;
        for (double e : arr)
            sum += e;
        return sum;
    }

    public static double sumArray(Number[] arr) {
        double sum = 0;
        for (Number e : arr)
            sum += e.doubleValue();
        return sum;
    }
}
