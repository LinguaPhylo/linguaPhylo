package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.parser.REPL;
import lphy.core.simulator.RandomUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MultinomialSeedTest {

    private static Integer[] sampleWithSeed(long seed) {
        RandomUtils.setSeed(seed);
        Multinomial multinomial = new Multinomial(new Value<>("n", 1000), new Value<>("p", new Double[]{0.2, 0.3, 0.5}));
        return multinomial.sample().value();
    }

    // the counts used to come from Mahout's own random number generator, ignoring LPhy's seed
    @Test
    public void testSeedIsReproducible() {
        Integer[] first = sampleWithSeed(42);
        assertArrayEquals(first, sampleWithSeed(42));
        assertFalse(Arrays.equals(first, sampleWithSeed(43)), "different seeds gave the same counts");
    }

    @Test
    public void testParsedSeedIsReproducible() {
        Integer[][] results = new Integer[2][];
        for (int i = 0; i < 2; i++) {
            RandomUtils.setSeed(7);
            REPL repl = new REPL();
            repl.parse("x ~ Multinomial(n=50, p=[0.2, 0.3, 0.5]);");
            results[i] = (Integer[]) repl.getModelDictionary().get("x").value();
        }
        assertArrayEquals(results[0], results[1]);
    }

    @Test
    public void testCountsSumToN() {
        RandomUtils.setSeed(1);
        Multinomial multinomial = new Multinomial(new Value<>("n", 37), new Value<>("p", new Double[]{0.1, 0.0, 0.6, 0.3}));
        for (int i = 0; i < 100; i++) {
            Integer[] x = multinomial.sample().value();
            assertEquals(37, Arrays.stream(x).mapToInt(Integer::intValue).sum());
            assertEquals(0, x[1], "an event with probability 0 occurred");
        }
    }

    @Test
    public void testZeroProbabilityLastEvent() {
        RandomUtils.setSeed(1);
        Multinomial multinomial = new Multinomial(new Value<>("n", 20), new Value<>("p", new Double[]{0.7, 0.3, 0.0}));
        for (int i = 0; i < 100; i++)
            assertEquals(0, multinomial.sample().value()[2]);
    }

    // E(x_i) = n p_i, sd of the mean over reps = sqrt(n p_i (1 - p_i) / reps); allow 5 sd
    @Test
    public void testMeans() {
        RandomUtils.setSeed(2024);
        int n = 200, reps = 2000;
        Double[] prob = {2.0, 3.0, 5.0}; // not normalised: same as {0.2, 0.3, 0.5}
        Multinomial multinomial = new Multinomial(new Value<>("n", n), new Value<>("p", prob));
        double[] sum = new double[prob.length];
        for (int r = 0; r < reps; r++) {
            Integer[] x = multinomial.sample().value();
            for (int i = 0; i < x.length; i++) sum[i] += x[i];
        }
        for (int i = 0; i < prob.length; i++) {
            double pi = prob[i] / 10.0;
            double sd = Math.sqrt(n * pi * (1 - pi) / reps);
            assertEquals(n * pi, sum[i] / reps, 5 * sd, "mean of x[" + i + "]");
        }
    }

    @Test
    public void testInvalidProbabilities() {
        assertThrows(IllegalArgumentException.class, () -> Multinomial.sample(RandomUtils.getRandom(), 5, new Double[]{0.5, -0.1}));
        assertThrows(IllegalArgumentException.class, () -> Multinomial.sample(RandomUtils.getRandom(), 5, new Double[]{0.0, 0.0}));
    }
}
