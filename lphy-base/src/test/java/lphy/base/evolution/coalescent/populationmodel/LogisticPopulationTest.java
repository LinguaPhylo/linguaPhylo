package lphy.base.evolution.coalescent.populationmodel;



import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogisticPopulationTest {

    private static final double DELTA = 1e-6;

    @Test
    public void testTheta() {
        // Parameters specific to your LogisticPopulation class
        double t50 = 10; // midpoint of the logistic growth curve
        double nCarryingCapacity = 1000; // carrying capacity of the population
        double b = 0.03; // growth rate

        // Instantiate the LogisticPopulation with your specific parameters
        LogisticPopulation population = new LogisticPopulation(t50, nCarryingCapacity, b);

        // Times at which to test the getTheta method
        double[] testTimes = {5, 10, 15}; // Selected times around t50 to test the growth curve's symmetry and limits

        // Expected Theta values at each test time based on the logistic growth equation
        double[] expectedThetaValues = {
                nCarryingCapacity / (1 + Math.exp(b * (testTimes[0] - t50))), // t = 5
                nCarryingCapacity / (1 + Math.exp(b * (testTimes[1] - t50))), // t = 10
                nCarryingCapacity / (1 + Math.exp(b * (testTimes[2] - t50)))  // t = 15
        };

        // Testing each time point
        for (int i = 0; i < testTimes.length; i++) {
            double actual = population.getTheta(testTimes[i]);
            System.out.printf("Testing getTheta at t = %.1f: Expected = %.3f, Actual = %.3f%n",
                    testTimes[i], expectedThetaValues[i], actual);
            assertEquals( testTimes[i], expectedThetaValues[i], actual, String.valueOf(DELTA));
        }
    }



    @Test
    public void testIntensityAndInverseIntensity() {
        // Parameters of the logistic growth model
        double t50 = 50;
        double nCarryingCapacity = 1000;
        double b = 0.1; // Assume growth rate b = 4 for this test

        // Initialize the LogisticPopulation with the specified parameters
        LogisticPopulation population = new LogisticPopulation(t50, nCarryingCapacity, b);

        // Print the intensity at time 0 for debugging purposes
        System.out.println("Intensity at 0: " + population.getIntensity(0.0));

        // Test a specific time point, for example, t = 2.0 (which is equal to t50 in this case)
        double t = 0.05;// 0.05;

        // Calculate the intensity at the given time point t
        double intensityAtTimeT = population.getIntensity(t);
        System.out.println("Passed intensity test! Intensity at time " + t + " is " + intensityAtTimeT);

        // Use the calculated intensity value to find the corresponding time using the inverse intensity function
        double inverseIntensityResult = population.getInverseIntensity(intensityAtTimeT);
        System.out.println("t = " + t);
        System.out.println("Intensity = " + intensityAtTimeT);
        System.out.println("Inverse Intensity = " + inverseIntensityResult);

        // Verify whether the inverse intensity calculation result is close to the original time point t
        assertEquals(t, inverseIntensityResult, DELTA, "Inverse intensity calculation should return the original time point within an acceptable error margin.");
    }





}
