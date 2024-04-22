package lphy.base.evolution.coalescent.populationmodel;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExponentialPopulationTest {
    private static final double DELTA = 1e-6;


    /**
     * Test the getTheta method of the ExponentialPopulation class.
     * This test verifies whether the getTheta method can correctly calculate theta value based on the exponential growth model.
     */
    @Test
    public void testGetTheta() {
        // Set the parameters for the exponential growth model
        double growthRate = 0.1; // Growth rate (r)
        double N0 = 100; // Initial population size

        ExponentialPopulation exponentialPopulation = new ExponentialPopulation(growthRate, N0);

        // Test getTheta method for a given time t
        double t = 80000; // Time at which to calculate theta
        // Calculate expected theta using the exponential growth formula: N0 * exp(growthRate * t)
        double expectedTheta = N0 * Math.exp(growthRate * -t);
        double actualTheta = exponentialPopulation.getTheta(t);

        // Assert that the expected value is equal to the actual value, allowing a certain error range
        assertEquals(expectedTheta, actualTheta, DELTA, "The theta value at time t should match the expected value calculated using the exponential growth formula.");
    }



    @Test
    public void testIntensityAndInverseIntensity() {
        // Parameters for the exponential growth model
        double growthRate = 0.1; // Growth rate
        double N0 = 100; // Initial population size

        ExponentialPopulation model = new ExponentialPopulation(growthRate, N0);

        double t = 2; // Time point to calculate intensity and then retrieve it through inverse intensity

        // Calculate cumulative intensity at a given point in time
        double intensityAtTimeT = model.getIntensity(t);

        // Use the accumulated intensity value to calculate its corresponding time
        double inverseIntensityResult = model.getInverseIntensity(intensityAtTimeT);

        // Output for debugging
        System.out.println("t = " + t);
        System.out.println("intensity = " + intensityAtTimeT);
        System.out.println("inverse intensity = " + inverseIntensityResult);

        // Verify whether the inverse intensity calculation result is close to the original time point
        assertEquals(t, inverseIntensityResult, DELTA, "Inverse intensity calculation should return the original time point within an acceptable error margin.");
    }


}


