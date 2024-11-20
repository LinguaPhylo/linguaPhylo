package lphy.base.evolution.coalescent.populationmodel;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogisticPopulationTest {

    private static final double DELTA = 1e-6;

    /**
     * Tests the getTheta method without ancestral population size (NA).
     */
    @Test
    public void testThetaWithoutNA() {
        // Parameter settings
        double t50 = 10.0; // Midpoint of the logistic growth curve
        double nCarryingCapacity = 1000.0; // Carrying capacity of the population
        double b = 0.03; // Growth rate

        // Instantiate LogisticPopulation without NA
        LogisticPopulation population = new LogisticPopulation(t50, nCarryingCapacity, b);

        // Test time points
        double[] testTimes = {5.0, 10.0, 15.0};

        // Expected Theta values based on the logistic growth equation
        double[] expectedThetaValues = {
                nCarryingCapacity / (1 + Math.exp(b * (testTimes[0] - t50))), // t = 5
                nCarryingCapacity / (1 + Math.exp(b * (testTimes[1] - t50))), // t = 10
                nCarryingCapacity / (1 + Math.exp(b * (testTimes[2] - t50)))  // t = 15
        };

        // Test each time point
        for (int i = 0; i < testTimes.length; i++) {
            double t = testTimes[i];
            double expected = expectedThetaValues[i];
            double actual = population.getTheta(t);
            System.out.printf("Testing getTheta without NA at t = %.1f: Expected = %.3f, Actual = %.3f%n",
                    t, expected, actual);
            assertEquals(expected, actual, DELTA, "Theta without NA should match the expected logistic value.");
        }
    }

    /**
     * Tests the getTheta method with ancestral population size (NA).
     */
    @Test
    public void testThetaWithNA() {
        // Parameter settings
        double t50 = 10.0; // Midpoint of the logistic growth curve
        double nCarryingCapacity = 1000.0; // Carrying capacity of the population
        double b = 0.03; // Growth rate
        double NA = 500.0; // Ancestral population size

        // Instantiate LogisticPopulation with NA
        LogisticPopulation population = new LogisticPopulation(t50, nCarryingCapacity, b, NA);

        // Test time points
        double[] testTimes = {5.0, 10.0, 15.0};

        // Expected Theta values based on the logistic growth equation with NA
        double[] expectedThetaValues = {
                NA + (nCarryingCapacity - NA) / (1 + Math.exp(b * (testTimes[0] - t50))), // t = 5
                NA + (nCarryingCapacity - NA) / (1 + Math.exp(b * (testTimes[1] - t50))), // t = 10
                NA + (nCarryingCapacity - NA) / (1 + Math.exp(b * (testTimes[2] - t50)))  // t = 15
        };

        // Test each time point
        for (int i = 0; i < testTimes.length; i++) {
            double t = testTimes[i];
            double expected = expectedThetaValues[i];
            double actual = population.getTheta(t);
            System.out.printf("Testing getTheta with NA at t = %.1f: Expected = %.3f, Actual = %.3f%n",
                    t, expected, actual);
            assertEquals(expected, actual, DELTA, "Theta with NA should match the expected logistic value considering ancestral population.");
        }
    }

    /**
     * Tests the getIntensity and getInverseIntensity methods without ancestral population size (NA).
     */
    @Test
    public void testIntensityAndInverseIntensityWithoutNA() {
        // Parameter settings
        double t50 = 50.0;
        double nCarryingCapacity = 1000.0;
        double b = 0.1;

        // Instantiate LogisticPopulation without NA
        LogisticPopulation population = new LogisticPopulation(t50, nCarryingCapacity, b);

        // Test time points
        double[] testTimes = {0.0, 25.0, 50.0, 75.0, 100.0};

        for (double t : testTimes) {
            double intensity = population.getIntensity(t);
            double inverseT = population.getInverseIntensity(intensity);
            System.out.printf("Testing Intensity and InverseIntensity without NA at t = %.2f: Intensity = %.6f, Inverse Intensity = %.6f%n",
                    t, intensity, inverseT);
            assertEquals(t, inverseT, DELTA, "Inverse intensity should return the original time point within an acceptable error margin.");
        }
    }

    /**
     * Tests the getIntensity and getInverseIntensity methods with ancestral population size (NA).
     */
    @Test
    public void testIntensityAndInverseIntensityWithNA() {
        // Parameter settings
        double t50 = 50.0;
        double nCarryingCapacity = 1000.0;
        double b = 0.1;
        double NA = 500.0;

        // Instantiate LogisticPopulation with NA
        LogisticPopulation population = new LogisticPopulation(t50, nCarryingCapacity, b, NA);

        // Test time points
        double[] testTimes = {0.0, 25.0, 50.0, 75.0, 100.0};

        for (double t : testTimes) {
            double intensity = population.getIntensity(t);
            double inverseT = population.getInverseIntensity(intensity);
            System.out.printf("Testing Intensity and InverseIntensity with NA at t = %.2f: Intensity = %.6f, Inverse Intensity = %.6f%n",
                    t, intensity, inverseT);
            assertEquals(t, inverseT, DELTA, "Inverse intensity should return the original time point within an acceptable error margin.");
        }
    }

    /**
     * Tests the toString method of LogisticPopulation with ancestral population size (NA).
     */
    @Test
    public void testToStringWithNA() {
        double t50 = 10.0;
        double nCarryingCapacity = 1000.0;
        double b = 0.03;
        double NA = 500.0;

        LogisticPopulation population = new LogisticPopulation(t50, nCarryingCapacity, b, NA);
        String expected = String.format(Locale.US, "Logistic Model with NA: t50=%.4f, nCarryingCapacity=%.4f, b=%.4f, NA=%.4f",
                t50, nCarryingCapacity, b, NA);
        String actual = population.toString();
        assertEquals(expected, actual);
    }

    /**
     * Tests the toString method of LogisticPopulation without ancestral population size (NA).
     */
    @Test
    public void testToStringWithoutNA() {
        double t50 = 10.0;
        double nCarryingCapacity = 1000.0;
        double b = 0.03;

        LogisticPopulation population = new LogisticPopulation(t50, nCarryingCapacity, b);
        String expected = String.format(Locale.US, "Logistic Model: t50=%.4f, nCarryingCapacity=%.4f, b=%.4f",
                t50, nCarryingCapacity, b);
        String actual = population.toString();
        assertEquals(expected, actual);
    }
}
