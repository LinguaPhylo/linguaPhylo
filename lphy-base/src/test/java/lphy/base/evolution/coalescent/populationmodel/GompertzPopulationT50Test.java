package lphy.base.evolution.coalescent.populationmodel;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GompertzPopulationT50Test {

    private static final double DELTA = 1e-6;
    final double NInfinity = 10000;  // Example carrying capacity
    final double t50 = 10.0;  // Example t50 value
    final double b = 0.1;     // Example growth rate
    final double NA = 10.000000002;
//
GompertzPopulation_t50 gompertzPopulation_t50 = new GompertzPopulation_t50(t50, b, NInfinity, NA);
    GompertzPopulation_t50 gompertzPopulation_t50WithoutNA = new GompertzPopulation_t50(t50, b, NInfinity);

    @Test
    public void testGetTheta() {
        double t = gompertzPopulation_t50.getTimeForGivenProportion(0.23456);


        double expectedTheta =NA + 0.23456 * (NInfinity - NA);
        double actualTheta = gompertzPopulation_t50.getTheta(t);
        assertEquals(expectedTheta, actualTheta, DELTA);
    }
    @Test
    public void testGetThetaWithoutNA() {
        double t = gompertzPopulation_t50WithoutNA.getTimeForGivenProportion(0.5);


        double expectedTheta =0.5 * (NInfinity);
        double actualTheta = gompertzPopulation_t50WithoutNA.getTheta(t);
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
    void testWithoutAncestralPopulation() {
        double t50 = 50.0;
        double b = 0.1;
        double NInfinity = 1000.0;

        GompertzPopulation_t50 model = new GompertzPopulation_t50(t50, b, NInfinity);

        double N0 = model.getN0();
        double N_at_0 = model.getTheta(0.0);
        assertEquals(N0, N_at_0, 1e-6, "N(t=0) should = N0");

        double N_at_t50 = model.getTheta(t50);
        assertEquals(NInfinity / 2.0, N_at_t50, 1e-6, "N(t50) should = NInfinity / 2");

        double largeTime = 1000.0;
        double N_at_largeTime = model.getTheta(largeTime);
        assertEquals(0, N_at_largeTime, 1e-3, "N(t) should near 0 when t -> +∞");
    }

    @Test
    void testWithAncestralPopulation() {
        double t50 = 20.0;
        double b = 0.1;
        double NInfinity = 10000.0;
        double NA = 100.0;

        GompertzPopulation_t50 model = new GompertzPopulation_t50(t50, b, NInfinity, NA);

        double N0 = model.getN0();
        double N_at_0 = model.getTheta(0.0);
        assertEquals(N0, N_at_0, 1e-6, "N(t=0) should =  N0");

        double expectedN_at_t50 = NA + (NInfinity - NA) / 2.0;
        double N_at_t50 = model.getTheta(t50);
        assertEquals(expectedN_at_t50, N_at_t50, 1e-6, "N(t50) should = NA + (NInfinity - NA) / 2");

        double largeTime = 1000.0;
        double N_at_largeTime = model.getTheta(largeTime);
        assertEquals(NA, N_at_largeTime, 1e-3, "N(t) should = NA when t -> +∞");
    }

    @Test
    void testCalculateN0WithNA() {
        double t50 = 20.0;
        double b = 0.1;
        double NInfinity = 10000.0;
        double NA = 100.0;

        double exponent = -Math.log(2) * Math.exp(-b * t50);
        double N0_minus_NA = (NInfinity - NA) * Math.exp(exponent);
        double expectedN0 = N0_minus_NA + NA;
        double calculatedN0 = GompertzPopulation_t50.calculateN0(t50, b, NInfinity, NA);

        assertEquals(expectedN0, calculatedN0, 1e-6, "calculateN0 with NA error ");
    }







}
