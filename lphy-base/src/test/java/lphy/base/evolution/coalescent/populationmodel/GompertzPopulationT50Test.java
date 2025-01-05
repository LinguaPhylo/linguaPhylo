package lphy.base.evolution.coalescent.populationmodel;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GompertzPopulationT50Test {

    private static final double DELTA = 1e-6;

    /**
     * Basic test verifying that, if I_na=1 and NA>0, the population uses NA.
     */
    @Test
    public void testGetThetaWithNA() {
        double t50 = 50.0;
        double b = 0.1;
        double NInfinity = 1000.0;
        double NA = 10.0;
        int I_na = 1;

        // Construct with NA usage
        GompertzPopulation_t50 model = new GompertzPopulation_t50(t50, b, NInfinity, NA, I_na);

        // Suppose we want 23.456% of the distance from NA to (NInfinity).
        double proportion = 0.23456;
        double expectedTheta = NA + proportion * (NInfinity - NA);

        // Get time at which the model is at that fraction
        double t = model.getTimeForGivenProportion(proportion);
        double actualTheta = model.getTheta(t);

        assertEquals(expectedTheta, actualTheta, DELTA, "Theta(t) should match NA + proportion*(N∞ - NA)");
        assertTrue(model.isUsingAncestralPopulation(), "isUsingAncestralPopulation() should be true when I_na=1 and NA>0.");
    }

    /**
     * If I_na=0 or NA<=0, the ancestral population is ignored.
     */
    @Test
    public void testGetThetaWithoutNA() {
        double t50 = 10.0;
        double b = 0.1;
        double NInfinity = 10000.0;
        double NA = 50.0; // would be ignored if I_na=0
        int I_na = 0;

        GompertzPopulation_t50 model = new GompertzPopulation_t50(t50, b, NInfinity, NA, I_na);

        // For proportion=0.5 => half of NInfinity
        double proportion = 0.5;
        double expectedTheta = proportion * NInfinity;

        double t = model.getTimeForGivenProportion(proportion);
        double actualTheta = model.getTheta(t);

        // NA should be ignored
        assertEquals(expectedTheta, actualTheta, DELTA, "Theta(t50) should match 0.5*N∞ if I_na=0.");
        assertTrue(!model.isUsingAncestralPopulation(), "Model should not be using NA if I_na=0.");
    }

    /**
     * Tests intensity numeric integration matches a direct numeric approach.
     */
    @Test
    public void testGetIntensity() {
        double t50 = 50.0;
        double b = 0.1;
        double NInfinity = 10000.0;
        double NA = 10.0;
        int I_na = 1;

        GompertzPopulation_t50 model = new GompertzPopulation_t50(t50, b, NInfinity, NA, I_na);
        double t = 5.0;

        // Numeric integration reference
        UnivariateFunction f = time -> 1.0 / model.getTheta(time);
        IterativeLegendreGaussIntegrator integrator =
                new IterativeLegendreGaussIntegrator(5, 1.0e-10, 1.0e-9, 2, 100000);
        double expected = integrator.integrate(Integer.MAX_VALUE, f, 0, t);

        double actual = model.getIntensity(t);
        assertEquals(expected, actual, DELTA, "Intensity(t) numeric check with NA usage.");
    }

    /**
     * Tests getInverseIntensity: the time at which Intensity(t)=x should return t.
     */
    @Test
    public void testGetInverseIntensity() {
        double t50 = 10.0;
        double b = 0.05;
        double NInfinity = 5000.0;
        double NA = 0.0;  // effectively no baseline
        int I_na = 1;     // but NA=0 => no effect

        GompertzPopulation_t50 model = new GompertzPopulation_t50(t50, b, NInfinity, NA, I_na);

        double t = 4.0;
        double intensity = model.getIntensity(t);

        double computedTime = model.getInverseIntensity(intensity);
        assertEquals(t, computedTime, 1e-2, "InverseIntensity(t) should retrieve t within a small tolerance.");
    }

    /**
     * Checks that if we construct with I_na=0, the model truly ignores the NA parameter.
     */
    @Test
    public void testIgnoreNA() {
        double t50 = 20.0;
        double b = 0.1;
        double NInfinity = 10000.0;
        double NA = 10.0;
        int I_na = 0;  // ignore NA

        GompertzPopulation_t50 model = new GompertzPopulation_t50(t50, b, NInfinity, NA, I_na);

        // For t50 => half of NInfinity
        double realT50 = model.getT50();
        double N_at_realT50 = model.getTheta(realT50);
        assertEquals(NInfinity*0.5, N_at_realT50, 1e-5,
                "N(t50) should be NInfinity/2 for no-NA scenario.");

    }

    /**
     * If we set a large time with I_na=1 and NA>0, model should approach NA as t->∞.
     */
    @Test
    public void testLongTimeApproachesNA() {
        double t50 = 55.0;
        double b = 0.1;
        double NInfinity = 2000.0;
        double NA = 10.0;
        int I_na = 1;

        GompertzPopulation_t50 model = new GompertzPopulation_t50(t50, b, NInfinity, NA, I_na);

        double largeTime = 1e5;
        double N_at_largeTime = model.getTheta(largeTime);
        // Should be close to NA
        assertEquals(NA, N_at_largeTime, 1.0, "As t->∞, N(t) should approach NA if I_na=1.");
    }

    /**
     * Verifies N0 calculation with an ancestral population.
     */
    @Test
    public void testCalculateN0WithNA() {
        double testT50 = 163.0;
        double testB = 0.018;
        double testNInfinity = 400.0;
        double testNA = 150.0;
        int testI_na = 1;

        GompertzPopulation_t50 model = new GompertzPopulation_t50(testT50, testB, testNInfinity, testNA, testI_na);

        // Cross-check the formula
        double exponent = -Math.log(2) / Math.exp(testB * testT50);
        double expectedN0 = (testNInfinity - testNA) * Math.exp(exponent) + testNA;
        double actualN0 = model.getN0();

        assertEquals(expectedN0, actualN0, DELTA, "N0 mismatch with NA usage.");
    }
}
