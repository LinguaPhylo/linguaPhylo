package lphy.base.evolution.coalescent;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BICEPSTest {

    private BICEPS createBICEPS(double alpha, double mean, Integer[] groupSizes, Double ploidy) {
        Value<Double> shape = new Value<>("populationShape", alpha);
        Value<Double> popMean = new Value<>("populationMean", mean);
        Value<Integer[]> gs = new Value<>("groupSizes", groupSizes);
        Value<Double> ploidyVal = ploidy != null ? new Value<>("ploidy", ploidy) : null;
        int n = 0;
        for (int g : groupSizes) n += g;
        n += 1;
        Value<Integer> nVal = new Value<>("n", n);
        return new BICEPS(shape, popMean, gs, ploidyVal, nVal, null, null);
    }

    @Test
    public void testSampleBasic() {
        // 10 taxa, 3 epochs of 3 coalescent events each
        BICEPS biceps = createBICEPS(3.0, 0.01, new Integer[]{3, 3, 3}, null);
        RandomVariable<TimeTree> result = biceps.sample();
        assertNotNull(result);
        TimeTree tree = result.value();
        assertNotNull(tree);
        assertEquals(10, tree.n(), "Tree should have 10 taxa");
        // Internal nodes = n-1 = 9, total nodes = 19
        assertEquals(19, tree.getNodes().size(), "Tree should have 19 nodes total");
    }

    @Test
    public void testSampleSingleEpoch() {
        // Single epoch with all coalescent events
        BICEPS biceps = createBICEPS(3.0, 0.01, new Integer[]{5}, null);
        RandomVariable<TimeTree> result = biceps.sample();
        assertNotNull(result);
        TimeTree tree = result.value();
        assertEquals(6, tree.n(), "Tree should have 6 taxa");
    }

    @Test
    public void testLogDensityFinite() {
        BICEPS biceps = createBICEPS(3.0, 0.01, new Integer[]{3, 3, 3}, null);
        RandomVariable<TimeTree> result = biceps.sample();
        TimeTree tree = result.value();

        double logDensity = biceps.logDensity(tree);
        assertTrue(Double.isFinite(logDensity), "logDensity should be finite, got: " + logDensity);
    }

    @Test
    public void testLogDensityConsistency() {
        BICEPS biceps = createBICEPS(3.0, 0.01, new Integer[]{4, 4, 4}, null);
        for (int i = 0; i < 100; i++) {
            RandomVariable<TimeTree> result = biceps.sample();
            double logDensity = biceps.logDensity(result.value());
            assertTrue(Double.isFinite(logDensity),
                    "logDensity should be finite on iteration " + i + ", got: " + logDensity);
        }
    }

    @Test
    public void testWithPloidy() {
        // Haploid ploidy = 0.5
        BICEPS biceps = createBICEPS(3.0, 0.01, new Integer[]{3, 3}, 0.5);
        RandomVariable<TimeTree> result = biceps.sample();
        assertNotNull(result);
        TimeTree tree = result.value();
        assertEquals(7, tree.n(), "Tree should have 7 taxa");

        double logDensity = biceps.logDensity(tree);
        assertTrue(Double.isFinite(logDensity), "logDensity with ploidy=0.5 should be finite");
    }

    @Test
    public void testInvalidAlpha() {
        assertThrows(IllegalArgumentException.class, () -> {
            createBICEPS(1.0, 0.01, new Integer[]{3, 3}, null);
        }, "alpha = 1.0 should throw IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, () -> {
            createBICEPS(0.5, 0.01, new Integer[]{3, 3}, null);
        }, "alpha = 0.5 should throw IllegalArgumentException");
    }

    @Test
    public void testNInconsistentWithGroupSizes() {
        // groupSizes sum to 6, so n should be 7, but we pass n=10
        Value<Double> shape = new Value<>("populationShape", 3.0);
        Value<Double> popMean = new Value<>("populationMean", 0.01);
        Value<Integer[]> gs = new Value<>("groupSizes", new Integer[]{3, 3});
        Value<Integer> nVal = new Value<>("n", 10);

        assertThrows(IllegalArgumentException.class, () -> {
            new BICEPS(shape, popMean, gs, null, nVal, null, null);
        }, "Mismatched n and groupSizes should throw IllegalArgumentException");
    }
}
