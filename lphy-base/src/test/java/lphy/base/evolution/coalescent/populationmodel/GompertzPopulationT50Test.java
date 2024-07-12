package lphy.base.evolution.coalescent.populationmodel;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GompertzPopulationT50Test {

    private static final double DELTA = 1e-6;
    final double NInfinity = 1000;  // Example carrying capacity
    final double t50 = 10.0;  // Example t50 value
    final double b = 0.1;     // Example growth rate

    GompertzPopulation_t50 gompertzPopulation_t50 = new GompertzPopulation_t50(t50, b, NInfinity);

    //TODO why it only fails in github action ?
//    @BeforeEach
//    public void setUp() {
//        gompertzPopulation_t50 = new GompertzPopulation_t50(t50, b, NInfinity);
//    }

    @Test
    public void testGetTheta() {
        double t = gompertzPopulation_t50.getTimeForGivenProportion(0.5);
        double N0 = gompertzPopulation_t50.getN0();

        double expectedTheta = NInfinity / 2;
        double actualTheta = gompertzPopulation_t50.getTheta(t);
        assertEquals(expectedTheta, actualTheta, DELTA);
    }

    @Test
    public void testGetThetaAtT50() {
        double expectedTheta = NInfinity / 2;
        double actualTheta = gompertzPopulation_t50.getTheta(t50);
        assertEquals(expectedTheta, actualTheta, DELTA);
    }

    @Test
    public void testGetIntensity() {
        double t = 5.0;
        UnivariateFunction function = time -> 1 / gompertzPopulation_t50.getTheta(time);
        IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(5, 1.0e-10, 1.0e-9, 2, 100000);
        double expectedIntensity = integrator.integrate(Integer.MAX_VALUE, function, 0, t);

        double actualIntensity = gompertzPopulation_t50.getIntensity(t);
        assertEquals(expectedIntensity, actualIntensity, DELTA);
    }

    @Test
    public void testGetInverseIntensity() {
        double t = 5.0;
        double intensity = gompertzPopulation_t50.getIntensity(t);

        double computedTime = gompertzPopulation_t50.getInverseIntensity(intensity);

        assertEquals(t, computedTime, DELTA);
    }

    @Test
    public void testInverseIntensity() {
        double targetIntensity = 0.309539;

        double calculatedTime = gompertzPopulation_t50.getInverseIntensity(targetIntensity);
        double calculatedIntensityAtCalculatedTime = gompertzPopulation_t50.getIntensity(calculatedTime);

        assertEquals(targetIntensity, calculatedIntensityAtCalculatedTime, DELTA);
    }

}
