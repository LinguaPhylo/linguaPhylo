package lphy.base.evolution.coalescent.populationmodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GompertzPopulationTest {

    private static final double DELTA = 1e-6;

    @Test
    public void testGetThetaAtTimeZero() {
        double N0 = 500;
        double b = 0.1;
        double NInfinity = 1000;
        GompertzPopulation gompertzPopulation = new GompertzPopulation(N0, b, NInfinity);

        double expectedThetaAtZero = N0;
        double resultThetaAtZero = gompertzPopulation.getTheta(0);
        assertEquals(expectedThetaAtZero, resultThetaAtZero, DELTA, "Theta at time zero should be equal to N0.");
    }

    @Test
    public void testGetTheta() {
        double N0 = 500;
        double b = 0.1;
        double NInfinity = 1000;
        double t = 10;  // Test at time t = 10
        GompertzPopulation gompertzPopulation = new GompertzPopulation(N0, b, NInfinity);

        double expectedTheta = N0 * Math.exp(Math.log(NInfinity / N0) * (1 - Math.exp(b * t)));
        double resultTheta = gompertzPopulation.getTheta(t);
        assertEquals(expectedTheta, resultTheta, DELTA, "Theta at time t should match the expected value.");
    }

    @Test
    public void testIntensityCalculation() {
        double N0 = 500;
        double b = 0.1;
        double NInfinity = 1000;
        double t = 5;  // Test at time t = 5
        GompertzPopulation gompertzPopulation = new GompertzPopulation(N0, b, NInfinity);

        double expectedIntensity = 0.012393784;
        double resultIntensity = gompertzPopulation.getIntensity(t);
        assertEquals(expectedIntensity, resultIntensity, DELTA, "Intensity calculation should match the expected value.");
    }

    @Test
    public void testInverseIntensity() {
        double N0 = 500;
        double b = 0.1;
        double NInfinity = 1000;
        double t = 5;  // Time for which we know the intensity
        GompertzPopulation gompertzPopulation = new GompertzPopulation(N0, b, NInfinity);

        // Calculate intensity at time t using getIntensity
        double intensity = gompertzPopulation.getIntensity(t);

        // Now calculate the time for the given intensity using getInverseIntensity
        double calculatedTime = gompertzPopulation.getInverseIntensity(intensity);

        // Assert that the time calculated by getInverseIntensity is close to the original time t
        assertEquals(t, calculatedTime, DELTA, "Inverse intensity calculation should return the correct time.");
    }

}



//package lphy.base.evolution.coalescent.populationmodel;
//
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//
//class GompertzPopulationTest {
//
//    private static final double DELTA = 1e-6;
//
//    @Test
//    public void testGetPopSize() {
//
//        // Set the parameters for the Gompertz growth model
//        double f0 = 0.5;  // Example initial proportion
//        double b = 0.1;   // Example growth rate
//        double NInfinity = 1000;  // Example carrying capacity
//
//        GompertzPopulation gompertzPopulation = new GompertzPopulation(f0, b, NInfinity);
//
//        double expectedTheta = gompertzPopulation.getN0() * Math.exp(Math.log(NInfinity / gompertzPopulation.getN0()) * (1 - Math.exp(b * 10)));
//        double result = gompertzPopulation.getTheta(10);
//        assertEquals( expectedTheta, result, DELTA);
//    }
//
//
//    @Test
//    public void testGetIntensity() {
//        // Set the parameters for the Gompertz growth model
//        double f0 = 0.1058;  // Example initial proportion
//        double b = 2.4982;   // Example growth rate
//        double NInfinity = 6016.7644;  // Example carrying capacity
//        GompertzPopulation gompertzPopulation = new GompertzPopulation(f0, b, NInfinity);
//        double t = 0.625;
//
//        double expectedIntensity = 0.309539; // Calculate based on the Gompertz model's Wolfram alpha
//
//        assertEquals(expectedIntensity, gompertzPopulation.getIntensity(t), DELTA, "Intensity calculation should be correct.");
//    }
//
//
//
//
//    @Test
//    public void testIntensityAndInverseIntensity() {
//        double f0 = 0.1058;  // Example initial proportion
//        double b = 2.4982;   // Example growth rate
//        double NInfinity = 6016.7644;  // Example carrying capacity
//        GompertzPopulation gompertzPopulation = new GompertzPopulation(f0, b, NInfinity);
//
//        double t = 0.625;
//
//        // Calculate intensity at given time t
//        double intensity = gompertzPopulation.getIntensity(t);
//
//        // Now calculate time for the given intensity, should approximately match t
//        double estimatedTime = gompertzPopulation.getInverseIntensity(0.309539);
//
//        // Assert that the time calculated by getInverseIntensity is close to the original time t
//        assertEquals(t, estimatedTime, DELTA);
//
//        // Optionally, verify the intensity value itself if you have an expected value
//        // Example expected value calculation (this would be your expected model output, based on your formula)
//        // double expectedIntensity = ...; // Depends on your intensity calculation model
//        // assertEquals("Intensity at time should match expected", expectedIntensity, intensity, DELTA);
//    }
//
//
//    @Test
//    public void testInverseIntensity() {
//        double f0 = 0.1058;
//        double b = 2.4982;
//        double NInfinity = 6016.7644;
//        GompertzPopulation gompertz = new GompertzPopulation(f0, b, NInfinity);
//
//        double targetIntensity = 0.309539; // The intensity for which we want to find the time
//        double expectedTime = 0.625; // The time at which the intensity should be targetIntensity
//
//        double calculatedTime = gompertz.getInverseIntensity(targetIntensity);
//        double calculatedIntensityAtCalculatedTime = gompertz.getIntensity(calculatedTime);
//
//        assertEquals(targetIntensity, calculatedIntensityAtCalculatedTime, DELTA);
//    }
//
//}
//
//
//
//
//
