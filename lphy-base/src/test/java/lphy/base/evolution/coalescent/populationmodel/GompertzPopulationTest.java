package lphy.base.evolution.coalescent.populationmodel;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class GompertzPopulationTest {

    private static final double DELTA = 1e-6;

    @Test
    public void testGetPopSize() {

        // Set the parameters for the Gompertz growth model
        double f0 = 0.5;  // Example initial proportion
        double b = 0.1;   // Example growth rate
        double NInfinity = 1000;  // Example carrying capacity

        GompertzPopulation gompertzPopulation = new GompertzPopulation(f0, b, NInfinity);

        double expectedTheta = gompertzPopulation.getN0() * Math.exp(Math.log(NInfinity / gompertzPopulation.getN0()) * (1 - Math.exp(b * 10)));
        double result = gompertzPopulation.getTheta(10);
        assertEquals( expectedTheta, result, DELTA);
    }


    @Test
    public void testGetIntensity() {
        // Set the parameters for the Gompertz growth model
        double f0 = 0.1058;  // Example initial proportion
        double b = 2.4982;   // Example growth rate
        double NInfinity = 6016.7644;  // Example carrying capacity
        GompertzPopulation gompertzPopulation = new GompertzPopulation(f0, b, NInfinity);
        double t = 0.625;

        double expectedIntensity = 0.309539; // Calculate based on the Gompertz model's Wolfram alpha

        assertEquals(expectedIntensity, gompertzPopulation.getIntensity(t), DELTA, "Intensity calculation should be correct.");
    }




    @Test
    public void testIntensityAndInverseIntensity() {
        double f0 = 0.1058;  // Example initial proportion
        double b = 2.4982;   // Example growth rate
        double NInfinity = 6016.7644;  // Example carrying capacity
        GompertzPopulation gompertzPopulation = new GompertzPopulation(f0, b, NInfinity);

        double t = 0.625;

        // Calculate intensity at given time t
        double intensity = gompertzPopulation.getIntensity(t);

        // Now calculate time for the given intensity, should approximately match t
        double estimatedTime = gompertzPopulation.getInverseIntensity(0.309539);

        // Assert that the time calculated by getInverseIntensity is close to the original time t
        assertEquals(t, estimatedTime, DELTA);

        // Optionally, verify the intensity value itself if you have an expected value
        // Example expected value calculation (this would be your expected model output, based on your formula)
        // double expectedIntensity = ...; // Depends on your intensity calculation model
        // assertEquals("Intensity at time should match expected", expectedIntensity, intensity, DELTA);
    }


    @Test
    public void testInverseIntensity() {
        double f0 = 0.1058;
        double b = 2.4982;
        double NInfinity = 6016.7644;
        GompertzPopulation gompertz = new GompertzPopulation(f0, b, NInfinity);

        double targetIntensity = 0.309539; // The intensity for which we want to find the time
        double expectedTime = 0.625; // The time at which the intensity should be targetIntensity

        double calculatedTime = gompertz.getInverseIntensity(targetIntensity);
        double calculatedIntensityAtCalculatedTime = gompertz.getIntensity(calculatedTime);

        assertEquals(targetIntensity, calculatedIntensityAtCalculatedTime, DELTA);
    }

}





