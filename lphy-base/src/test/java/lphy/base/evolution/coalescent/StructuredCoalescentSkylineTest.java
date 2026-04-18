package lphy.base.evolution.coalescent;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;
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
                logNe2x2(), mConstant2Demes(), null, rateShifts2Epochs(), null,
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
                logNe2x2(), mConstant2Demes(), null, rateShifts2Epochs(), null,
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
                new Value<>("logNe", badLogNe), mConstant2Demes(), null, rateShifts2Epochs(), null,
                new Value<>("taxa", taxa), new Value<>("demes", demes), null));
    }

    @Test
    public void rejectLogNeWrongInnerDim() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};

        // 3 epochs but rateShifts says 2 epochs
        Double[][] badLogNe = {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}};

        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                new Value<>("logNe", badLogNe), mConstant2Demes(), null, rateShifts2Epochs(), null,
                new Value<>("taxa", taxa), new Value<>("demes", demes), null));
    }

    @Test
    public void rejectMWrongSize() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};

        // 3 migration rates for K=2 (should be 2)
        Value<Double[]> badM = new Value<>("M", new Double[]{0.1, 0.2, 0.3});

        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                logNe2x2(), badM, null, rateShifts2Epochs(), null,
                new Value<>("taxa", taxa), new Value<>("demes", demes), null));
    }

    @Test
    public void rejectRateShiftsNotIncreasing() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};

        Value<Double[]> badShifts = new Value<>("rateShifts", new Double[]{1.0, 0.5});

        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                logNe2x2(), mConstant2Demes(), null, badShifts, null,
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
                null,
                rateShifts2Epochs(),
                null,
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
                logNe2x2(), mConstant2Demes(), null, rateShifts2Epochs(), null,
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
                logNe2x2(), mConstant2Demes(), null, rateShifts2Epochs(), null,
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
                        null,
                        new Value<>("rateShifts", rateShiftsVal),
                        null,
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
                    null,
                    new Value<>("rateShifts", rateShiftsVal),
                    null,
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
                        null,
                        new Value<>("rateShifts", rateShiftsVal),
                        null,
                        new Value<>("taxa", taxa),
                        new Value<>("demes", demes),
                        new Value<>("interpolation", mode));
                samples[rep] = gen.sample().value().getRoot().getAge();
            }
            assertMeanWithinSE(samples, expected, "C " + mode);
        }
    }

    // ------------------------------------------------------------------
    // Time-varying migration tests
    // ------------------------------------------------------------------

    /**
     * 2 demes, 1 lineage per deme, Ne = 1 constant, symmetric migration
     * switching at τ=1 from m1=0.5 to m2=2.0. Expected T_MRCA comes from the
     * 3-state absorbing CTMC (S=separate, T=together, A=coalesced) with Q1
     * for t∈[0,τ) and Q2 for t≥τ. The non-absorbing 2×2 restricted generator is
     * <pre>
     *      |  -2m     2m    |
     *      |   2m   -(1+2m) |
     * </pre>
     * so E[T_MRCA] = 1'·( Q1^{-1}(exp(Q1 τ) − I) p0  −  Q2^{-1} exp(Q1 τ) p0 )
     * with p0 = [1, 0]'.
     */
    @Test
    public void expectedTmrca_twoDemes_phaseSplitMigration_constant() {
        double m1 = 0.5, m2 = 2.0, tau = 1.0;
        double expected = phaseSplitTmrca(m1, m2, tau);

        Taxa taxa = Taxa.createTaxa(2);
        Object[] demes = new Object[]{"A", "B"};
        Double[][] logNeVal = {{0.0, 0.0}, {0.0, 0.0}};
        Double[] rateShiftsVal = {0.0, tau};
        // logM layout: [A→B, B→A] × [epoch 0, epoch 1]; symmetric m1 then m2.
        Double[][] logMVal = {
                {Math.log(m1), Math.log(m2)},
                {Math.log(m1), Math.log(m2)}
        };
        Double[] migShiftsVal = {0.0, tau};

        double[] samples = new double[MC_REPS];
        for (int rep = 0; rep < MC_REPS; rep++) {
            StructuredCoalescentSkyline gen = new StructuredCoalescentSkyline(
                    new Value<>("logNe", logNeVal),
                    null,
                    new Value<>("logM", logMVal),
                    new Value<>("rateShifts", rateShiftsVal),
                    new Value<>("migrationRateShifts", migShiftsVal),
                    new Value<>("taxa", taxa),
                    new Value<>("demes", demes),
                    new Value<>("interpolation", StructuredCoalescentSkyline.INTERP_CONSTANT));
            samples[rep] = gen.sample().value().getRoot().getAge();
        }
        assertMeanWithinSE(samples, expected, "phase-split constant");
    }

    /**
     * E[T_MRCA] for the 2-deme, 1-per-deme, Ne=1 phase-split CTMC.
     * Same setup as above; derivation in the test comment.
     */
    private static double phaseSplitTmrca(double m1, double m2, double tau) {
        RealMatrix Q1 = buildPhaseQ(m1);
        RealMatrix Q2 = buildPhaseQ(m2);
        RealMatrix expQ1tau = matrixExp(Q1.scalarMultiply(tau));
        RealVector p0 = new ArrayRealVector(new double[]{1.0, 0.0});
        RealVector pTau = expQ1tau.operate(p0);

        RealMatrix I = MatrixUtils.createRealIdentityMatrix(2);
        RealMatrix Q1inv = MatrixUtils.inverse(Q1);
        RealMatrix Q2inv = MatrixUtils.inverse(Q2);

        RealVector phase1Integral = Q1inv.operate(expQ1tau.subtract(I).operate(p0));
        RealVector phase2Integral = Q2inv.operate(pTau).mapMultiply(-1.0);

        RealVector totalIntegral = phase1Integral.add(phase2Integral);
        return totalIntegral.getEntry(0) + totalIntegral.getEntry(1);
    }

    private static RealMatrix buildPhaseQ(double m) {
        return new Array2DRowRealMatrix(new double[][]{
                {-2.0 * m, 2.0 * m},
                {2.0 * m, -(1.0 + 2.0 * m)}
        });
    }

    /**
     * Matrix exponential via eigendecomposition. Sufficient for the 2×2
     * real-diagonalisable restricted generators used here.
     */
    private static RealMatrix matrixExp(RealMatrix A) {
        EigenDecomposition ed = new EigenDecomposition(A);
        RealMatrix V = ed.getV();
        RealMatrix D = ed.getD();
        double[] expEig = new double[D.getRowDimension()];
        for (int i = 0; i < expEig.length; i++) expEig[i] = Math.exp(D.getEntry(i, i));
        RealMatrix expD = MatrixUtils.createRealDiagonalMatrix(expEig);
        return V.multiply(expD).multiply(MatrixUtils.inverse(V));
    }

    /**
     * Linear-interpolation smoke test with time-varying log-migration.
     * Just asserts the simulator returns a valid tree with correct deme
     * assignments; the analytical expectation for piecewise-linear logM is
     * harder to derive, so we check correctness indirectly.
     */
    @Test
    public void linearInterpolationTimeVaryingMigrationSamples() {
        Taxa taxa = Taxa.createTaxa(6);
        Object[] demes = new Object[]{"A", "A", "A", "B", "B", "B"};
        Double[][] logNeVal = {{0.0, 0.0}, {0.0, 0.0}};
        Double[] rateShiftsVal = {0.0, 1.0};
        Double[][] logMVal = {
                {Math.log(0.1), Math.log(1.0)},
                {Math.log(0.1), Math.log(1.0)}
        };
        Double[] migShiftsVal = {0.0, 0.5};

        StructuredCoalescentSkyline gen = new StructuredCoalescentSkyline(
                new Value<>("logNe", logNeVal),
                null,
                new Value<>("logM", logMVal),
                new Value<>("rateShifts", rateShiftsVal),
                new Value<>("migrationRateShifts", migShiftsVal),
                new Value<>("taxa", taxa),
                new Value<>("demes", demes),
                new Value<>("interpolation", StructuredCoalescentSkyline.INTERP_LINEAR));

        assertEquals(true, gen.isLinearInterpolation());
        assertEquals(true, gen.isTimeVaryingMigration());
        assertEquals(2, gen.getNMigEpochs());

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

    /**
     * Supplying both M and logM must throw IllegalArgumentException.
     */
    @Test
    public void rejectBothMAndLogM() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};
        Double[][] logMVal = {{Math.log(0.1), Math.log(0.2)}, {Math.log(0.1), Math.log(0.2)}};
        Double[] migShiftsVal = {0.0, 1.0};

        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                logNe2x2(),
                mConstant2Demes(),
                new Value<>("logM", logMVal),
                rateShifts2Epochs(),
                new Value<>("migrationRateShifts", migShiftsVal),
                new Value<>("taxa", taxa),
                new Value<>("demes", demes),
                null));
    }

    /**
     * Supplying neither M nor logM must throw IllegalArgumentException.
     */
    @Test
    public void rejectNeitherMNorLogM() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};
        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                logNe2x2(), null, null, rateShifts2Epochs(), null,
                new Value<>("taxa", taxa), new Value<>("demes", demes), null));
    }

    /**
     * Supplying logM without migrationRateShifts must throw.
     */
    @Test
    public void rejectLogMWithoutShifts() {
        Taxa taxa = Taxa.createTaxa(4);
        Object[] demes = new Object[]{"A", "A", "B", "B"};
        Double[][] logMVal = {{Math.log(0.1), Math.log(0.2)}, {Math.log(0.1), Math.log(0.2)}};
        assertThrows(IllegalArgumentException.class, () -> new StructuredCoalescentSkyline(
                logNe2x2(), null,
                new Value<>("logM", logMVal),
                rateShifts2Epochs(),
                null,
                new Value<>("taxa", taxa), new Value<>("demes", demes), null));
    }
}
