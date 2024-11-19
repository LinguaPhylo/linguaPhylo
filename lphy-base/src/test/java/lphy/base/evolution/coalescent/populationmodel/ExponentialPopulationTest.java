package lphy.base.evolution.coalescent.populationmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExponentialPopulationTest {
    private static final double DELTA = 1e-6;

    private double growthRate;
    private double N0;
    private double NA;
    private ExponentialPopulation expPop;
    private ExponentialPopulation expPopWithNA;

    /**
     * Setup method to initialize common test parameters and instances before each test.
     */
    @BeforeEach
    public void setUp() {
        // Initialize parameters
        growthRate = 0.1; // Growth rate (r)
        N0 = 100.0;        // Initial population size
        NA = 50.0;         // Ancestral population size

        // Create instances
        expPop = new ExponentialPopulation(growthRate, N0);
        expPopWithNA = new ExponentialPopulation(growthRate, N0, NA);
    }

    /**
     * Test the getTheta method without using Ancestral Population (NA).
     * This test verifies whether the getTheta method correctly calculates theta based on the exponential growth model without NA.
     */
    @Test
    @DisplayName("Test getTheta without Ancestral Population (NA)")
    public void testGetThetaWithoutNA() {
        double t = 10.0; // Time at which to calculate theta

        // Calculate expected theta using the exponential growth formula: N0 * exp(-r * t)
        double expectedTheta = N0 * Math.exp(-growthRate * t);
        double actualTheta = expPop.getTheta(t);

        // Assert that the expected value is equal to the actual value within the specified delta
        assertEquals(expectedTheta, actualTheta, DELTA, "Theta without NA should match the expected exponential decay value.");
    }

    /**
     * Test the getTheta method with using Ancestral Population (NA).
     * This test verifies whether the getTheta method correctly calculates theta based on the exponential growth model with NA.
     */
    @Test
    @DisplayName("Test getTheta with Ancestral Population (NA)")
    public void testGetThetaWithNA() {
        double t = 10.0; // Time at which to calculate theta

        // Calculate expected theta using the exponential growth formula with NA:
        // N(t) = (N0 - NA) * exp(-r * t) + NA
        double expectedTheta = (N0 - NA) * Math.exp(-growthRate * t) + NA;
        double actualTheta = expPopWithNA.getTheta(t);

        // Assert that the expected value is equal to the actual value within the specified delta
        assertEquals(expectedTheta, actualTheta, DELTA, "Theta with NA should match the expected value considering ancestral population.");
    }

    /**
     * Test the getIntensity and getInverseIntensity methods without using Ancestral Population (NA).
     * This test verifies the consistency between intensity and its inverse without NA.
     */
    @Test
    @DisplayName("Test Intensity and InverseIntensity without Ancestral Population (NA)")
    public void testIntensityAndInverseIntensityWithoutNA() {
        double t = 2.0; // Time point to calculate intensity and then retrieve it through inverse intensity

        // Calculate cumulative intensity at time t
        double intensityAtTimeT = expPop.getIntensity(t);

        // Use the accumulated intensity value to calculate its corresponding time
        double inverseIntensityResult = expPop.getInverseIntensity(intensityAtTimeT);

        // Output for debugging
        System.out.println("Without NA:");
        System.out.println("t = " + t);
        System.out.println("intensity = " + intensityAtTimeT);
        System.out.println("inverse intensity = " + inverseIntensityResult);

        // Assert that the inverse intensity result is equal to the original time within the specified delta
        assertEquals(t, inverseIntensityResult, DELTA, "Inverse intensity should return the original time without NA.");
    }

    /**
     * Test the getIntensity and getInverseIntensity methods with using Ancestral Population (NA).
     * This test verifies the consistency between intensity and its inverse with NA.
     */
    @Test
    @DisplayName("Test Intensity and InverseIntensity with Ancestral Population (NA)")
    public void testIntensityAndInverseIntensityWithNA() {
        double t = 122.000007; // Time point to calculate intensity and then retrieve it through inverse intensity

        // Calculate cumulative intensity at time t with NA
        double intensityAtTimeT = expPopWithNA.getIntensity(t);

        // Use the accumulated intensity value to calculate its corresponding time
        double inverseIntensityResult = expPopWithNA.getInverseIntensity(intensityAtTimeT);

        // Output for debugging
        System.out.println("With NA:");
        System.out.println("t = " + t);
        System.out.println("intensity = " + intensityAtTimeT);
        System.out.println("inverse intensity = " + inverseIntensityResult);

        // Assert that the inverse intensity result is equal to the original time within the specified delta
        assertEquals(t, inverseIntensityResult, DELTA, "Inverse intensity should return the original time with NA.");
    }

    /**
     * Test the getInverseIntensity method with a range of intensity values with NA.
     * This test ensures that inverseIntensity correctly retrieves the original time for various intensities.
     */
    @Test
    @DisplayName("Test InverseIntensity with multiple intensity values with NA")
    public void testInverseIntensityMultipleWithNA() {
        double[] testTimes = {0.0, 5.0, 10.0, 20.0, 50.0, 100.0}; // Various time points

        for (double t : testTimes) {
            // Calculate cumulative intensity at time t with NA
            double intensity = expPopWithNA.getIntensity(t);

            // Use the accumulated intensity value to calculate its corresponding time
            double inverseIntensityResult = expPopWithNA.getInverseIntensity(intensity);

            // Output for debugging
            System.out.println("With NA:");
            System.out.println("t = " + t);
            System.out.println("intensity = " + intensity);
            System.out.println("inverse intensity = " + inverseIntensityResult);

            // Assert that the inverse intensity result is equal to the original time within the specified delta
            assertEquals(t, inverseIntensityResult, DELTA, "Inverse intensity should return the original time for multiple points with NA.");
        }
    }
}
