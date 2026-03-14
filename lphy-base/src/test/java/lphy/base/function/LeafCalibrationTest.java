package lphy.base.function;

import lphy.base.evolution.DateToAge;
import lphy.base.evolution.tree.LeafCalibrations;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


public class LeafCalibrationTest {

    private static final String NEXUS_CONTENT = """
            #NEXUS
            
            BEGIN DATA;
                DIMENSIONS NTAX=7 NCHAR=68;
                FORMAT MISSING=? GAP=- DATATYPE=DNA;
                MATRIX
                D4Mexico_1984      ATGCGATGCGTAGGAGTAGGAAACAGAGACTTTGTGGAAGGAGTCTCAGGTGGAGCATGGGTCGACCT
                D4ElSal_1994       ATGCGATGCGTAGGAGTAGGAAACAGAGACTTTGTGGAAGGAGTCTCAGGTGGAGCATGGGTCGACCT
                D4Philip_1984      ATGCGATGCGTAGGAGTGGGGAACAGAGACTTTGTGGAAGGAGTCTCAGGTGGAGCATGGGTCGACTT
                D4Tahiti_1985      ATGCGATGCGTAGGAGTAGGAAACAGAGACTTTGTGGAAGGAGTTTCAGGTGGAGCATGGGTCGATTT
                D4PRico_1986       ATGCGATGCGTAGGAGTAGGAAACAGAGACTTTGTGGAAGGAGTCTCAGGTGGAGCATGGGTCGACCT
                D4Thai_1984        ATGCGATGCGTAGGAGTAGGGAACAGAGACTTTGTAGAAGGAGTCTCAGGTGGAGCATGGGTCGATCT
                D4Brazi_1982       ATGCGATGCGTAGGAGTAGGAAACAGAGACTTTGTGGAAGGAGTCTCAGGTGGAGCATGGGTCGACCT
                ;
            END;
            
            BEGIN ASSUMPTIONS;
                OPTIONS SCALE = years;
            
                CALIBRATE D4ElSal_1994  = fixed(1994),
                CALIBRATE D4PRico_1986  = uniform(1984,1988),
                CALIBRATE D4Tahiti_1985 = offsetexponential(1984,1),
                CALIBRATE D4Mexico_1984 = normal(1984,5),
                CALIBRATE D4Philip_1984 = lognormal(1984,1.5),
                CALIBRATE D4Thai_1984   = offsetlognormal(1980,4,1.25),
                CALIBRATE D4Brazi_1982  = offsetgamma(1980,2,1);
            
            END;
            """;

    // MATRIX order is the ground truth — indices 0..6
    // index 0 = D4Mexico_1984  → normal(1984,5)              stochastic
    // index 1 = D4ElSal_1994   → fixed(1994)                 fixed
    // index 2 = D4Philip_1984  → lognormal(1984,1.5)         stochastic
    // index 3 = D4Tahiti_1985  → offsetexponential(1984,1)   stochastic
    // index 4 = D4PRico_1986   → uniform(1984,1988)          stochastic
    // index 5 = D4Thai_1984    → offsetlognormal(1980,4,1.25)stochastic
    // index 6 = D4Brazi_1982   → offsetgamma(1980,2,1)       stochastic
    private static final String[] MATRIX_ORDER = {
            "D4Mexico_1984",
            "D4ElSal_1994",
            "D4Philip_1984",
            "D4Tahiti_1985",
            "D4PRico_1986",
            "D4Thai_1984",
            "D4Brazi_1982"
    };

    // Only fixed() entries get a concrete expected value; stochastic entries are NaN
    private static final double[] EXPECTED_FIXED_AGES = {
            Double.NaN,  // index 0 - D4Mexico_1984  = normal(1984,5)
            1994.0,      // index 1 - D4ElSal_1994   = fixed(1994)
            Double.NaN,  // index 2 - D4Philip_1984  = lognormal(1984,1.5)
            Double.NaN,  // index 3 - D4Tahiti_1985  = offsetexponential(1984,1)
            Double.NaN,  // index 4 - D4PRico_1986   = uniform(1984,1988)
            Double.NaN,  // index 5 - D4Thai_1984    = offsetlognormal(1980,4,1.25)
            Double.NaN,  // index 6 - D4Brazi_1982   = offsetgamma(1980,2,1)
    };

    private Path tempNexusFile;

    @BeforeEach
    void setUp() throws IOException {
        tempNexusFile = Files.createTempFile("calibrations_test", ".nexus");
        Files.writeString(tempNexusFile, NEXUS_CONTENT);
        // Clear static map so tests never bleed into each other
        LeafCalibrations.TipCalibration.clearCalibrations();
    }

    // output array length matches number of taxa in MATRIX
    @Test
    void testOutputLengthMatchesTaxaCount() {
        LeafCalibrations lc = new LeafCalibrations(new Value<>("file", tempNexusFile.toString()));
        Double[] ages = lc.sample().value();

        assertEquals(MATRIX_ORDER.length, ages.length,
                "Output ages array length should match number of taxa in the MATRIX block");
    }

    // fixed value occur at right index
    @Test
    void testFixedAgesAreInMatrixOrder() {
        LeafCalibrations lc = new LeafCalibrations(new Value<>("file", tempNexusFile.toString()));
        Double[] ages = lc.sample().value();

        for (int i = 0; i < EXPECTED_FIXED_AGES.length; i++) {
            if (!Double.isNaN(EXPECTED_FIXED_AGES[i])) {
                assertEquals(EXPECTED_FIXED_AGES[i], ages[i], 1e-9,
                        "Wrong age at index " + i + " (taxon: " + MATRIX_ORDER[i] + ")");
            }
        }
    }

    // output order is same as alignment chunk
    @Test
    void testCalibrationsListMatchesMatrixOrder() {
        LeafCalibrations lc = new LeafCalibrations(new Value<>("file", tempNexusFile.toString()));
        lc.sample();

        List<LeafCalibrations.TipCalibration> calibrations = lc.getCalibrations();

        assertNotNull(calibrations, "Calibrations list should not be null after sample()");
        assertEquals(MATRIX_ORDER.length, calibrations.size(),
                "Calibrations list size should match taxa count");

        for (int i = 0; i < MATRIX_ORDER.length; i++) {
            assertEquals(MATRIX_ORDER[i], calibrations.get(i).taxonName,
                    "Calibration at index " + i + " should be '" + MATRIX_ORDER[i]
                            + "' but was '" + calibrations.get(i).taxonName + "'");
        }
    }

    // drawing from right distribution - one test per distribution type
    @Test
    void testDistributionRange() {
        // Run multiple samples to reduce flakiness for stochastic distributions
        final int N = 100;

        for (int run = 0; run < N; run++) {
            LeafCalibrations lc = new LeafCalibrations(new Value<>("file", tempNexusFile.toString()));
            Double[] ages = lc.sample().value();
            List<LeafCalibrations.TipCalibration> calibrations = lc.getCalibrations();

            // index 0 - D4Mexico_1984 = normal(1984, 5)
            // normal has no hard bounds, but >3 sigma from mean is astronomically unlikely
            assertEquals("D4Mexico_1984", calibrations.get(0).taxonName);
            assertEquals("normal(1984.0, 5.0)", LeafCalibrations.TipCalibration.getDistribution("D4Mexico_1984"));
//            assertTrue(ages[0] > 1984.0 - 15.0 && ages[0] < 1984.0 + 15.0,
//                    "D4Mexico_1984 normal(1984,5) sample out of 3-sigma range: " + ages[0]);

            // index 1 - D4ElSal_1994 = fixed(1994)
            // fixed must always return exactly the specified value
            assertEquals("D4ElSal_1994", calibrations.get(1).taxonName);
            assertEquals("fixed(1994.0)", LeafCalibrations.TipCalibration.getDistribution("D4ElSal_1994"));
            assertEquals(1994.0, ages[1], 1e-16,
                    "D4ElSal_1994 fixed(1994) must always return exactly 1994.0");

            // index 2 - D4Philip_1984 = lognormal(1984, 1.5)
            // lognormal is always positive, and with these params overwhelmingly > 0
            assertEquals("D4Philip_1984", calibrations.get(2).taxonName);
            assertEquals("lognormal(1984.0, 1.5)", LeafCalibrations.TipCalibration.getDistribution("D4Philip_1984"));
//            assertTrue(ages[2] > 0.0,
//                    "D4Philip_1984 lognormal sample must be positive: " + ages[2]);

            // index 3 - D4Tahiti_1985 = offsetexponential(1984, 1)
            // offset exponential must always be strictly greater than the offset
            assertEquals("D4Tahiti_1985", calibrations.get(3).taxonName);
            assertEquals("offsetexponential(1984.0, 1.0)", LeafCalibrations.TipCalibration.getDistribution("D4Tahiti_1985"));
            assertTrue(ages[3] > 1984.0,
                    "D4Tahiti_1985 offsetexponential(1984,1) sample must be > 1984: " + ages[3]);

            // index 4 - D4PRico_1986 = uniform(1984, 1988)
            // uniform must always be within [lo, hi]
            assertEquals("D4PRico_1986", calibrations.get(4).taxonName);
            assertEquals("uniform(1984.0, 1988.0)", LeafCalibrations.TipCalibration.getDistribution("D4PRico_1986"));
            assertTrue(ages[4] >= 1984.0 && ages[4] <= 1988.0,
                    "D4PRico_1986 uniform(1984,1988) sample out of [1984,1988]: " + ages[4]);

            // index 5 - D4Thai_1984 = offsetlognormal(1980, 4, 1.25)
            // offset lognormal must always be strictly greater than the offset
            assertEquals("D4Thai_1984", calibrations.get(5).taxonName);
            assertEquals("offsetlognormal(1980.0, 4.0, 1.25)", LeafCalibrations.TipCalibration.getDistribution("D4Thai_1984"));
            assertTrue(ages[5] > 1980.0,
                    "D4Thai_1984 offsetlognormal(1980,4,1.25) sample must be > 1980: " + ages[5]);

            // index 6 - D4Brazi_1982 = offsetgamma(1980, 2, 1)
            // offset gamma must always be strictly greater than the offset
            assertEquals("D4Brazi_1982", calibrations.get(6).taxonName);
            assertEquals("offsetgamma(1980.0, 2.0, 1.0)", LeafCalibrations.TipCalibration.getDistribution("D4Brazi_1982"));
            assertTrue(ages[6] > 1980.0,
                    "D4Brazi_1982 offsetgamma(1980,2,1) sample must be > 1980: " + ages[6]);

            // reset between runs so the static calibrationMap is clean
            LeafCalibrations.TipCalibration.clearCalibrations();
        }
    }
    // throw error when get input file as unexpected format
    @Test
    void testInvalidFileExtensionThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new LeafCalibrations(new Value<>("file", "calibrations.txt")),
                "Should throw for non-nexus file extension");
    }

    @Test
    void testAgeConversion() {
        assertThrows(IllegalArgumentException.class, () -> new DateToAge(null), "Should throw for null dates input");

        Double[] dates = {1994.0, 1986.0, 1978.0, 1963.0};
        DateToAge f = new DateToAge(new Value<>(null, dates));
        Double[] result = f.apply().value();
        assertEquals(dates.length, result.length, "Output length should match input length");
        assertEquals(0.0, result[0], 1e-9, "Most recent date (1994) should map to age 0.0");

        Double[] expected = {0.0, 8.0, 16.0, 31.0};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result[i], 1e-9,
                    "Wrong relative age at index " + i + " for date " + dates[i]);
        }
    }

    @Test
    void testAllSameDateBecomesAllZero() {
        // If all dates are the same, all relative ages should be 0
        Double[] dates = {1984.0, 1984.0, 1984.0};
        DateToAge f = new DateToAge(new Value<>(null, dates));
        Double[] result = f.apply().value();

        for (int i = 0; i < result.length; i++) {
            assertEquals(0.0, result[i], 1e-9,
                    "All same dates should produce all zero ages");
        }
    }

    @Test
    void testSingleDateBecomesZero() {
        // A single date should always map to age 0
        Double[] dates = {1984.0};
        DateToAge f = new DateToAge(new Value<>(null, dates));
        Double[] result = f.apply().value();

        assertEquals(1, result.length);
        assertEquals(0.0, result[0], 1e-9,
                "Single date should map to age 0.0");
    }

    @Test
    void testOrderPreserved() {
        // Output should be in the same index order as input
        Double[] dates = {1963.0, 1978.0, 1984.0, 1986.0, 1994.0};
        DateToAge f = new DateToAge(new Value<>(null, dates));
        Double[] result = f.apply().value();

        // max = 1994
        assertEquals(31.0, result[0], 1e-9, "1963 → 31");
        assertEquals(16.0, result[1], 1e-9, "1978 → 16");
        assertEquals(10.0, result[2], 1e-9, "1984 → 10");
        assertEquals(8.0,  result[3], 1e-9, "1986 → 8");
        assertEquals(0.0,  result[4], 1e-9, "1994 → 0");
    }

    @Test
    void testNonIntegerDates() {
        // Should work correctly with fractional dates e.g. 1984.5
        Double[] dates  = {1984.5, 1983.0, 1980.25};
        Double[] expected = {0.0, 1.5, 4.25};
        DateToAge f = new DateToAge(new Value<>(null, dates));
        Double[] result = f.apply().value();

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result[i], 1e-9,
                    "Wrong relative age at index " + i + " for fractional date " + dates[i]);
        }
    }
}