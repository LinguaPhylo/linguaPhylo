package lphy.base.evolution.coalescent.populationmodel;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for the updated LogisticPopulation class,
 * which now includes an indicator iNa (0 or 1) to control NA usage.
 */
public class LogisticPopulationTest {

    private static final double DELTA = 1e-6;

    /**
     * Tests getTheta(t) without NA usage (iNa=0).
     * The logistic equation should behave as if NA=0.0.
     */
    @Test
    public void testThetaWithoutNA() {
        double t50 = 10.0;
        double carryingCapacity = 1000.0;
        double b = 0.03;
        double NA = 500.0; // but will be ignored if iNa=0
        int iNa = 0;

        // Creates a logistic population ignoring NA
        LogisticPopulation population = new LogisticPopulation(t50, carryingCapacity, b, NA, iNa);

        double[] times = {5.0, 10.0, 15.0};
        for (double t : times) {
            // Expected: K/(1+exp[b*(t - t50)])
            double expected = carryingCapacity / (1.0 + Math.exp(b * (t - t50)));
            double actual = population.getTheta(t);
            assertEquals(expected, actual, DELTA,
                    "Theta(t) without NA should match the standard logistic formula.");
        }
        assertFalse(population.isUsingAncestralPopulation(),
                "iNa=0 => the model must not use NA.");
    }

    /**
     * Tests getTheta(t) with NA usage (iNa=1, NA>0).
     * The logistic equation should incorporate NA as a baseline.
     */
    @Test
    public void testThetaWithNA() {
        double t50 = 10.0;
        double carryingCapacity = 1000.0;
        double b = 0.03;
        double NA = 500.0;
        int iNa = 1; // use NA

        LogisticPopulation population = new LogisticPopulation(t50, carryingCapacity, b, NA, iNa);

        double[] times = {5.0, 10.0, 15.0};
        for (double t : times) {
            // Expected: NA + (K - NA)/(1+exp[b*(t - t50)])
            double expected = NA + (carryingCapacity - NA)
                    / (1.0 + Math.exp(b * (t - t50)));
            double actual = population.getTheta(t);
            assertEquals(expected, actual, DELTA,
                    "Theta(t) with NA must follow NA + (K - NA)/(1+exp(...)).");
        }
        assertTrue(population.isUsingAncestralPopulation(),
                "iNa=1 and NA>0 => the model should be using NA.");
    }

    /**
     * Tests intensity and inverseIntensity without NA (iNa=0).
     * InverseIntensity should recover the original time, within tolerance.
     */
    @Test
    public void testIntensityAndInverseIntensityWithoutNA() {
        double t50 = 50.0;
        double carryingCapacity = 1000.0;
        double b = 0.1;
        double NA = 500.0; // ignored
        int iNa = 0;       // skip NA

        LogisticPopulation population = new LogisticPopulation(t50, carryingCapacity, b, NA, iNa);

        double[] testTimes = {0.0, 25.0, 50.0, 75.0, 100.0};
        for (double t : testTimes) {
            double intensity = population.getIntensity(t);
            double recoveredT = population.getInverseIntensity(intensity);
            assertEquals(t, recoveredT, DELTA,
                    "Inverse intensity with iNa=0 must retrieve the original time.");
        }
    }

    /**
     * Tests intensity and inverseIntensity with NA usage (iNa=1, NA>0).
     */
    @Test
    public void testIntensityAndInverseIntensityWithNA() {
        double t50 = 50.0;
        double carryingCapacity = 1000.0;
        double b = 0.1;
        double NA = 500.0;
        int iNa = 1;

        LogisticPopulation population = new LogisticPopulation(t50, carryingCapacity, b, NA, iNa);

        double[] testTimes = {0.0, 25.0, 50.0, 75.0, 100.0};
        for (double t : testTimes) {
            double intensity = population.getIntensity(t);
            double recoveredT = population.getInverseIntensity(intensity);
            assertEquals(t, recoveredT, DELTA,
                    "Inverse intensity with NA must retrieve the original time within tolerance.");
        }
    }

    /**
     * Tests the toString output when using NA (iNa=1).
     */
    @Test
    public void testToStringWithNA() {
        double t50 = 10.0;
        double carryingCapacity = 1000.0;
        double b = 0.03;
        double NA = 500.0;
        int iNa = 1;

        LogisticPopulation population = new LogisticPopulation(t50, carryingCapacity, b, NA, iNa);
        String expected = String.format(Locale.US,
                "LogisticPopulation [t50=%.4f, K=%.4f, b=%.4f, NA=%.4f, iNa=%d]",
                t50, carryingCapacity, b, NA, iNa);
        String actual = population.toString();
        assertEquals(expected, actual, "toString with NA usage must match the expected format.");
    }

    /**
     * Tests the toString output when ignoring NA (iNa=0).
     */
    @Test
    public void testToStringWithoutNA() {
        double t50 = 10.0;
        double carryingCapacity = 1000.0;
        double b = 0.03;
        double NA = 500.0; // ignored
        int iNa = 0;

        LogisticPopulation population = new LogisticPopulation(t50, carryingCapacity, b, NA, iNa);
        String expected = String.format(Locale.US,
                "LogisticPopulation [t50=%.4f, K=%.4f, b=%.4f, NA=%.4f, iNa=%d] (NA ignored)",
                t50, carryingCapacity, b, 0.0, iNa);
        // note: once iNa=0 => we effectively store NA=0.0 in the constructor

        String actual = population.toString();
        assertEquals(expected, actual, "toString without NA usage must match the expected format.");
    }
}
