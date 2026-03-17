package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoissonIndicatorsTest {

    @Test
    void testSampleLength() {
        Value<Integer> n = new Value<>("n", 10);
        Value<Number> lambda = new Value<>("lambda", 2.0);

        PoissonIndicators dist = new PoissonIndicators(n, lambda);
        RandomVariable<Boolean[]> result = dist.sample();
        Boolean[] indicators = result.value();

        assertEquals(10, indicators.length, "Should produce array of length n");
    }

    @Test
    void testSampleCountBounded() {
        Value<Integer> n = new Value<>("n", 5);
        Value<Number> lambda = new Value<>("lambda", 1.0);

        PoissonIndicators dist = new PoissonIndicators(n, lambda);

        for (int trial = 0; trial < 100; trial++) {
            RandomVariable<Boolean[]> result = dist.sample();
            Boolean[] indicators = result.value();
            int count = 0;
            for (boolean b : indicators) {
                if (b) count++;
            }
            assertTrue(count >= 0 && count <= 5,
                    "Number of active indicators should be between 0 and n");
        }
    }

    @Test
    void testLogDensityValid() {
        Value<Integer> n = new Value<>("n", 5);
        Value<Number> lambda = new Value<>("lambda", 2.0);

        PoissonIndicators dist = new PoissonIndicators(n, lambda);

        Boolean[] x = {true, false, true, false, false}; // k=2
        double logP = dist.logDensity(x);
        assertTrue(Double.isFinite(logP), "Log density should be finite for valid input");
        assertTrue(logP < 0, "Log density should be negative");
    }

    @Test
    void testLogDensityWrongLength() {
        Value<Integer> n = new Value<>("n", 5);
        Value<Number> lambda = new Value<>("lambda", 2.0);

        PoissonIndicators dist = new PoissonIndicators(n, lambda);

        Boolean[] x = {true, false, true}; // length 3 != 5
        double logP = dist.logDensity(x);
        assertEquals(Double.NEGATIVE_INFINITY, logP,
                "Log density should be -inf for wrong length");
    }

    @Test
    void testLogDensitySymmetry() {
        // Two configurations with the same k should have the same density
        Value<Integer> n = new Value<>("n", 4);
        Value<Number> lambda = new Value<>("lambda", 1.5);

        PoissonIndicators dist = new PoissonIndicators(n, lambda);

        Boolean[] x1 = {true, true, false, false}; // k=2
        Boolean[] x2 = {false, true, false, true};  // k=2

        double logP1 = dist.logDensity(x1);
        double logP2 = dist.logDensity(x2);

        assertEquals(logP1, logP2, 1e-10,
                "Configurations with same k should have same density (uniform placement)");
    }

    @Test
    void testLogDensityNormalization() {
        // Sum over all possible configurations should equal 1 (within numerical precision)
        int n = 3;
        Value<Integer> nVal = new Value<>("n", n);
        Value<Number> lambda = new Value<>("lambda", 1.0);

        PoissonIndicators dist = new PoissonIndicators(nVal, lambda);

        // Enumerate all 2^3 = 8 configurations
        double totalProb = 0.0;
        for (int mask = 0; mask < (1 << n); mask++) {
            Boolean[] x = new Boolean[n];
            for (int i = 0; i < n; i++) {
                x[i] = ((mask >> i) & 1) == 1;
            }
            totalProb += Math.exp(dist.logDensity(x));
        }

        assertEquals(1.0, totalProb, 1e-8,
                "Total probability over all configurations should be 1.0");
    }

    @Test
    void testGetParams() {
        Value<Integer> n = new Value<>("n", 5);
        Value<Number> lambda = new Value<>("lambda", 1.0);

        PoissonIndicators dist = new PoissonIndicators(n, lambda);

        assertEquals(2, dist.getParams().size());
        assertSame(n, dist.getParams().get("n"));
        assertSame(lambda, dist.getParams().get("lambda"));
    }
}
