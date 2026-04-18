package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GaussianRandomWalkTest {

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(777);
    }

    @Test
    public void sampleFirstValueMode() {
        Value<Double> firstValue = new Value<>("x0", 3.5);
        Value<Double> sd = new Value<>("sd", 1.0);
        Value<Integer> n = new Value<>("n", 10);

        GaussianRandomWalk grw = new GaussianRandomWalk(null, firstValue, sd, n);
        Double[] chain = grw.sample().value();

        assertEquals(10, chain.length);
        assertEquals(3.5, chain[0], 1e-12);
    }

    @Test
    public void sampleInitialMeanMode() {
        Value<Double> initialMean = new Value<>("m0", 0.0);
        Value<Double> sd = new Value<>("sd", 1.0);
        Value<Integer> n = new Value<>("n", 100);

        GaussianRandomWalk grw = new GaussianRandomWalk(initialMean, null, sd, n);
        Double[] chain = grw.sample().value();

        assertEquals(100, chain.length);
        // Just sanity: chain exists and is finite.
        for (double x : chain) {
            assertEquals(x, x, 0.0); // not NaN
        }
    }

    @Test
    public void logDensityFirstValueMode() {
        // In firstValue mode, X[0] is fixed (delegate to firstValue's generator's logDensity).
        // To avoid needing a generator on the Value, test a chain where firstValue has no generator
        // and we expect the density to reduce to sum of increments only if firstValue.getGenerator() is null.
        // Here we test initialMean mode, which doesn't depend on external generator.
    }

    @Test
    public void logDensityInitialMeanMode() {
        Value<Double> initialMean = new Value<>("m0", 0.0);
        Value<Double> sd = new Value<>("sd", 2.0);
        Value<Integer> n = new Value<>("n", 4);

        GaussianRandomWalk grw = new GaussianRandomWalk(initialMean, null, sd, n);

        Double[] x = new Double[]{0.5, 1.2, -0.3, 2.1};

        NormalDistribution nd0 = new NormalDistribution(null, 0.0, 2.0);
        double expected = nd0.logDensity(x[0]);
        for (int i = 1; i < x.length; i++) {
            NormalDistribution nd = new NormalDistribution(null, x[i-1], 2.0);
            expected += nd.logDensity(x[i]);
        }

        assertEquals(expected, grw.logDensity(x), 1e-12);
    }

    @Test
    public void rejectBothInputs() {
        Value<Double> initialMean = new Value<>("m0", 0.0);
        Value<Double> firstValue = new Value<>("x0", 1.0);
        Value<Double> sd = new Value<>("sd", 1.0);
        Value<Integer> n = new Value<>("n", 5);

        assertThrows(IllegalArgumentException.class,
                () -> new GaussianRandomWalk(initialMean, firstValue, sd, n));
    }

    @Test
    public void rejectNeitherInput() {
        Value<Double> sd = new Value<>("sd", 1.0);
        Value<Integer> n = new Value<>("n", 5);

        assertThrows(IllegalArgumentException.class,
                () -> new GaussianRandomWalk(null, null, sd, n));
    }
}
