package lphy.base.evolution.coalescent;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StructuredCoalescentSkylineTest {

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(777);
    }

    private Value<Double[][]> logNe2x2() {
        Double[][] logNe = {
                {Math.log(1.0), Math.log(1.0)},   // deme A (alphabetical first)
                {Math.log(2.0), Math.log(0.5)}    // deme B
        };
        return new Value<>("logNe", logNe);
    }

    private Value<Double[]> mConstant2Demes() {
        // K=2 -> K*(K-1) = 2 rates: A->B, B->A
        return new Value<>("M", new Double[]{0.1, 0.2});
    }

    private Value<Double[]> rateShifts2Epochs() {
        return new Value<>("rateShifts", new Double[]{0.0, 1.0});
    }

    @Test
    public void sampleProducesValidTree() {
        Taxa taxa = Taxa.createTaxa(6);
        Object[] demes = new Object[]{"A", "A", "A", "B", "B", "B"};

        StructuredCoalescentSkyline gen = new StructuredCoalescentSkyline(
                logNe2x2(), mConstant2Demes(), rateShifts2Epochs(),
                new Value<>("taxa", taxa), new Value<>("demes", demes), null);

        assertEquals(2, gen.getNDemes());
        assertEquals(2, gen.getNEpochs());
        assertEquals(Arrays.asList("A", "B"), gen.getUniqueDemes());

        RandomVariable<TimeTree> treeRV = gen.sample();
        assertNotNull(treeRV);

        TimeTree tree = treeRV.value();
        List<TimeTreeNode> tips = tree.getExtantNodes();
        assertEquals(6, tips.size());

        // Each tip should have a deme metadata matching the input demes array
        for (TimeTreeNode tip : tips) {
            String taxonId = tip.getId();
            int i = taxa.indexOfTaxon(taxonId);
            Object demeMeta = tip.getMetaData(StructuredCoalescentSkyline.populationLabel);
            assertEquals(String.valueOf(demes[i]), demeMeta, "tip " + taxonId);
        }
    }

    @Test
    public void alphabeticalSort() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"B", "B", "A", "A"};

        StructuredCoalescentSkyline gen = new StructuredCoalescentSkyline(
                logNe2x2(), mConstant2Demes(), rateShifts2Epochs(),
                new Value<>("taxa", taxa), new Value<>("demes", demes), null);

        // Alphabetical: A first, B second.
        assertEquals(Arrays.asList("A", "B"), gen.getUniqueDemes());
    }

    @Test
    public void rejectLogNeWrongOuterDim() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};

        // Only 1 row, but 2 demes
        Double[][] badLogNe = {{0.0, 0.0}};

        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                new Value<>("logNe", badLogNe), mConstant2Demes(), rateShifts2Epochs(),
                new Value<>("taxa", taxa), new Value<>("demes", demes), null));
    }

    @Test
    public void rejectLogNeWrongInnerDim() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};

        // 3 epochs but rateShifts says 2 epochs
        Double[][] badLogNe = {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}};

        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                new Value<>("logNe", badLogNe), mConstant2Demes(), rateShifts2Epochs(),
                new Value<>("taxa", taxa), new Value<>("demes", demes), null));
    }

    @Test
    public void rejectMWrongSize() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};

        // 3 migration rates for K=2 (should be 2)
        Value<Double[]> badM = new Value<>("M", new Double[]{0.1, 0.2, 0.3});

        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                logNe2x2(), badM, rateShifts2Epochs(),
                new Value<>("taxa", taxa), new Value<>("demes", demes), null));
    }

    @Test
    public void rejectRateShiftsNotIncreasing() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};

        Value<Double[]> badShifts = new Value<>("rateShifts", new Double[]{1.0, 0.5});

        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                logNe2x2(), mConstant2Demes(), badShifts,
                new Value<>("taxa", taxa), new Value<>("demes", demes), null));
    }

    @Test
    public void threeDemes() {
        Taxa taxa = Taxa.createTaxa(6);
        Object[] demes = new Object[]{"A", "A", "B", "B", "C", "C"};

        // K=3, n=2 epochs
        Double[][] logNe = {
                {0.0, 0.0},   // A
                {0.0, 0.0},   // B
                {0.0, 0.0}    // C
        };
        // K*(K-1) = 6 migration rates: A->B, A->C, B->A, B->C, C->A, C->B
        Double[] M = {0.1, 0.1, 0.1, 0.1, 0.1, 0.1};

        StructuredCoalescentSkyline gen = new StructuredCoalescentSkyline(
                new Value<>("logNe", logNe),
                new Value<>("M", M),
                rateShifts2Epochs(),
                new Value<>("taxa", taxa),
                new Value<>("demes", demes), null);

        assertEquals(3, gen.getNDemes());
        RandomVariable<TimeTree> treeRV = gen.sample();
        assertNotNull(treeRV);
        assertEquals(6, treeRV.value().getExtantNodes().size());
    }

    @Test
    public void linearInterpolationSamples() {
        Taxa taxa = Taxa.createTaxa(6);
        Object[] demes = new Object[]{"A", "A", "A", "B", "B", "B"};

        StructuredCoalescentSkyline gen = new StructuredCoalescentSkyline(
                logNe2x2(), mConstant2Demes(), rateShifts2Epochs(),
                new Value<>("taxa", taxa), new Value<>("demes", demes),
                new Value<>("interpolation", StructuredCoalescentSkyline.INTERP_LINEAR));

        assertEquals(true, gen.isLinearInterpolation());

        RandomVariable<TimeTree> treeRV = gen.sample();
        assertNotNull(treeRV);
        assertEquals(6, treeRV.value().getExtantNodes().size());

        for (TimeTreeNode tip : treeRV.value().getExtantNodes()) {
            String taxonId = tip.getId();
            int i = taxa.indexOfTaxon(taxonId);
            Object demeMeta = tip.getMetaData(StructuredCoalescentSkyline.populationLabel);
            assertEquals(String.valueOf(demes[i]), demeMeta, "tip " + taxonId);
        }
    }

    @Test
    public void rejectUnknownInterpolation() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};

        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                logNe2x2(), mConstant2Demes(), rateShifts2Epochs(),
                new Value<>("taxa", taxa), new Value<>("demes", demes),
                new Value<>("interpolation", "piecewiseCubic")));
    }

    // ------------------------------------------------------------------
    // Analytical validation tests: each one has a closed-form expectation
    // for E[T_MRCA] that the simulator must reproduce. Rather than a fixed
    // tolerance, we compute the sample SE at runtime and reject if the
    // empirical mean deviates by more than SE_TOLERANCE_SIGMA standard
    // errors from the analytical expectation. With 4σ, a correct simulator
    // fails ~1 in 16000 — low enough to not be flaky, tight enough that a
    // real bias larger than ~2 SE will be caught at high power.
    // ------------------------------------------------------------------

    private static final int MC_REPS = 20000;
    private static final double SE_TOLERANCE_SIGMA = 4.0;

    /**
     * Check that the sample mean over the supplied replicates is within
     * SE_TOLERANCE_SIGMA sample standard errors of the analytical expectation.
     * Uses a two-pass computation — mean first, then variance — so the
     * numerical error from Welford-style updates is not a concern at this N.
     */
    private static void assertMeanWithinSE(double[] samples, double expected, String label) {
        int n = samples.length;
        double mean = 0.0;
        for (double s : samples) mean += s;
        mean /= n;
        double sumSq = 0.0;
        for (double s : samples) sumSq += (s - mean) * (s - mean);
        double sampleVar = sumSq / (n - 1);
        double se = Math.sqrt(sampleVar / n);
        double tol = SE_TOLERANCE_SIGMA * se;
        double diff = mean - expected;
        if (Math.abs(diff) > tol) {
            throw new AssertionError(String.format(
                    "%s: mean %.5f differs from expected %.5f by %+.5f = %.2fσ (tol %.2fσ)",
                    label, mean, expected, diff, diff / se, SE_TOLERANCE_SIGMA));
        }
    }

    /**
     * 2 lineages, 1 deme, Ne = 1 constant. T_MRCA ~ Exp(1), E[T] = 1.
     * Should hold under either interpolation mode (with flat log-Ne the
     * linear simulator always hits the B = 0 branch).
     */
    @Test
    public void expectedTmrca_singleDeme_constantNe() {
        Taxa taxa = Taxa.createTaxa(2);
        Object[] demes = new Object[]{"A", "A"};
        Double[][] logNeVal = {{0.0, 0.0}};
        Double[] mVal = {};
        Double[] rateShiftsVal = {0.0, 1.0};
        double expected = 1.0;

        for (String mode : new String[]{StructuredCoalescentSkyline.INTERP_CONSTANT,
                                         StructuredCoalescentSkyline.INTERP_LINEAR}) {
            double[] samples = new double[MC_REPS];
            for (int rep = 0; rep < MC_REPS; rep++) {
                StructuredCoalescentSkyline gen = new StructuredCoalescentSkyline(
                        new Value<>("logNe", logNeVal),
                        new Value<>("M", mVal),
                        new Value<>("rateShifts", rateShiftsVal),
                        new Value<>("taxa", taxa),
                        new Value<>("demes", demes),
                        new Value<>("interpolation", mode));
                samples[rep] = gen.sample().value().getRoot().getAge();
            }
            assertMeanWithinSE(samples, expected, "A " + mode);
        }
    }

    /**
     * 2 lineages, 1 deme, linear log-Ne with slope b = -1 over a long
     * segment, so Ne(t) = exp(-t) within the segment. Coalescence is
     * essentially certain before the segment ends (P(T > 50) ≈ exp(-e^50)).
     * Griffiths & Tavaré 1994: E[T_MRCA] = e · E1(1), where E1(1) is the
     * exponential integral. E1(1) = 0.219383934..., so E[T] ≈ 0.596347.
     */
    @Test
    public void expectedTmrca_singleDeme_exponentialGrowth_linear() {
        Taxa taxa = Taxa.createTaxa(2);
        Object[] demes = new Object[]{"A", "A"};
        // Slope = (-50 - 0) / 50 = -1 → Ne(t) = exp(-t) within [0, 50].
        Double[][] logNeVal = {{0.0, -50.0}};
        Double[] mVal = {};
        Double[] rateShiftsVal = {0.0, 50.0};
        double expected = Math.E * 0.21938393439552024; // e · E1(1)

        double[] samples = new double[MC_REPS];
        for (int rep = 0; rep < MC_REPS; rep++) {
            StructuredCoalescentSkyline gen = new StructuredCoalescentSkyline(
                    new Value<>("logNe", logNeVal),
                    new Value<>("M", mVal),
                    new Value<>("rateShifts", rateShiftsVal),
                    new Value<>("taxa", taxa),
                    new Value<>("demes", demes),
                    new Value<>("interpolation", StructuredCoalescentSkyline.INTERP_LINEAR));
            samples[rep] = gen.sample().value().getRoot().getAge();
        }
        assertMeanWithinSE(samples, expected, "B linear");
    }

    /**
     * 2 lineages, 2 demes (1 per deme), Ne = 1 constant, symmetric
     * migration rate m = 1. Solving the 2-state absorbing CTMC
     * (separate ↔ together → coalesced) gives E[T_MRCA] = 5/2 = 2.5.
     * Both interpolation modes should give this since logNe is flat.
     */
    @Test
    public void expectedTmrca_twoDemes_symmetricMigration() {
        Taxa taxa = Taxa.createTaxa(2);
        Object[] demes = new Object[]{"A", "B"};
        Double[][] logNeVal = {{0.0, 0.0}, {0.0, 0.0}};
        Double[] mVal = {1.0, 1.0}; // m_AB = m_BA = 1
        Double[] rateShiftsVal = {0.0, 1.0};
        double expected = 2.5;

        for (String mode : new String[]{StructuredCoalescentSkyline.INTERP_CONSTANT,
                                         StructuredCoalescentSkyline.INTERP_LINEAR}) {
            double[] samples = new double[MC_REPS];
            for (int rep = 0; rep < MC_REPS; rep++) {
                StructuredCoalescentSkyline gen = new StructuredCoalescentSkyline(
                        new Value<>("logNe", logNeVal),
                        new Value<>("M", mVal),
                        new Value<>("rateShifts", rateShiftsVal),
                        new Value<>("taxa", taxa),
                        new Value<>("demes", demes),
                        new Value<>("interpolation", mode));
                samples[rep] = gen.sample().value().getRoot().getAge();
            }
            assertMeanWithinSE(samples, expected, "C " + mode);
        }
    }
}
