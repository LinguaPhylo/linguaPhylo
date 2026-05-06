package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Properties every empirical amino acid Q matrix must satisfy:
 * (1) row sums are zero,
 * (2) off-diagonal entries are non-negative,
 * (3) reversibility — pi_i Q_ij = pi_j Q_ji (computed via stationary distribution),
 * (4) mean rate (sum_i -pi_i Q_ii) equals 1 by default.
 * Plus a cross-check of LG row 0 against the values in Le &amp; Gascuel 2008 supplementary
 * data: A→{R,N,D,C} exchangeabilities equal {0.425093, 0.276818, 0.395144, 2.489084}.
 */
class EmpiricalAminoAcidModelTest {

    private static final double TOL = 1e-12;

    static Stream<EmpiricalAminoAcidModel> allModels() {
        return Stream.of(
                new WAG(null, null),
                new LG(null, null),
                new JTT(null, null),
                new Dayhoff(null, null),
                new DCMut(null, null),
                new Blosum62(null, null),
                new VT(null, null),
                new CpREV(null, null),
                new MtREV(null, null),
                new MtMam(null, null),
                new MtArt(null, null),
                new RtREV(null, null),
                new FLU(null, null),
                new HIVb(null, null),
                new HIVw(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("allModels")
    void rowSumsZero(EmpiricalAminoAcidModel model) {
        Double[][] Q = model.apply().value();
        for (int i = 0; i < 20; i++) {
            double rowSum = 0;
            for (int j = 0; j < 20; j++) rowSum += Q[i][j];
            assertEquals(0.0, rowSum, TOL,
                    model.getClass().getSimpleName() + " row " + i + " does not sum to 0");
        }
    }

    @ParameterizedTest
    @MethodSource("allModels")
    void offDiagonalsNonNegative(EmpiricalAminoAcidModel model) {
        Double[][] Q = model.apply().value();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (i != j) {
                    assertTrue(Q[i][j] >= -TOL,
                            model.getClass().getSimpleName() + " Q[" + i + "][" + j + "] is negative: " + Q[i][j]);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("allModels")
    void meanRateIsOne(EmpiricalAminoAcidModel model) {
        // pi_i = -Q_ji / Q_jj * pi_j ... reconstruct stationary distribution from
        // the empirical frequencies (alphabetical order, via permutation in base class).
        double[] piPaml = model.getEmpiricalFrequenciesPAML();
        double[] pi = new double[20];
        for (int i = 0; i < 20; i++) pi[EmpiricalAminoAcidModel.PAML_TO_ALPHA[i]] = piPaml[i];

        Double[][] Q = model.apply().value();
        double meanRate = 0;
        for (int i = 0; i < 20; i++) meanRate += -Q[i][i] * pi[i];
        assertEquals(1.0, meanRate, 1e-10,
                model.getClass().getSimpleName() + " mean rate not normalised to 1");
    }

    @ParameterizedTest
    @MethodSource("allModels")
    void reversibility(EmpiricalAminoAcidModel model) {
        double[] piPaml = model.getEmpiricalFrequenciesPAML();
        double[] pi = new double[20];
        for (int i = 0; i < 20; i++) pi[EmpiricalAminoAcidModel.PAML_TO_ALPHA[i]] = piPaml[i];

        Double[][] Q = model.apply().value();
        for (int i = 0; i < 20; i++) {
            for (int j = i + 1; j < 20; j++) {
                assertEquals(pi[i] * Q[i][j], pi[j] * Q[j][i], 1e-12,
                        model.getClass().getSimpleName() + " not reversible at (" + i + "," + j + ")");
            }
        }
    }

    /**
     * Cross-check: the four lowest A→X exchangeabilities published in Le &amp; Gascuel
     * (2008) supplementary data file LG.dat are A→R=0.425093, A→N=0.276818,
     * A→D=0.395144 and A→C=2.489084. After multiplying by pi_X and the global
     * scale factor 1/meanRate, the ratios Q[A][X] / (pi_X * exchangeability) must
     * be the same constant for all X.
     */
    @Test
    void lgValuesAgreeWithLeGascuel2008() {
        LG lg = new LG(null, null);
        Double[][] Q = lg.apply().value();
        // alphabetical AA order: A=0, C=1, D=2, ..., R=14
        double piR = 0.055941, piN = 0.041977, piD = 0.053052, piC = 0.012937;
        double exchAR = 0.425093, exchAN = 0.276818, exchAD = 0.395144, exchAC = 2.489084;
        double[] kRaw = {
                Q[0][14] / (piR * exchAR),
                Q[0][11] / (piN * exchAN),
                Q[0][2]  / (piD * exchAD),
                Q[0][1]  / (piC * exchAC)
        };
        for (int i = 1; i < kRaw.length; i++) {
            assertEquals(kRaw[0], kRaw[i], 1e-10,
                    "LG exchangeability ratio mismatch at index " + i + ": " + kRaw[i] + " vs " + kRaw[0]);
        }
    }

    /**
     * The new framework places state index 17 = V (alphabetical AA order). The
     * largest A→X exchangeability in LG is A→V (2.547870), so Q[A][V] should
     * dominate the high-frequency entries in row A.
     */
    @Test
    void lgAlphabeticalOrderingIsCorrect() {
        LG lg = new LG(null, null);
        Double[][] Q = lg.apply().value();
        // V should have a substantially larger Q[A][V] than W or Y
        assertTrue(Q[0][17] > Q[0][18],
                "Expected Q[A][V]=" + Q[0][17] + " > Q[A][W]=" + Q[0][18]);
        assertTrue(Q[0][17] > Q[0][19],
                "Expected Q[A][V]=" + Q[0][17] + " > Q[A][Y]=" + Q[0][19]);
    }

    @Test
    void userSuppliedFrequenciesOverrideEmpirical() {
        Double[] uniform = new Double[20];
        for (int i = 0; i < 20; i++) uniform[i] = 0.05;
        WAG wag = new WAG(new Value<>("freq", uniform), null);
        Double[][] Q = wag.apply().value();
        // mean rate must still be 1.0 against the user-supplied uniform pi
        double meanRate = 0;
        for (int i = 0; i < 20; i++) meanRate += -Q[i][i] * 0.05;
        assertEquals(1.0, meanRate, 1e-10);
        // reversibility against uniform pi: Q[i][j] should equal Q[j][i]
        for (int i = 0; i < 20; i++) {
            for (int j = i + 1; j < 20; j++) {
                assertEquals(Q[i][j], Q[j][i], 1e-12,
                        "WAG with uniform freqs must be symmetric at (" + i + "," + j + ")");
            }
        }
    }
}
