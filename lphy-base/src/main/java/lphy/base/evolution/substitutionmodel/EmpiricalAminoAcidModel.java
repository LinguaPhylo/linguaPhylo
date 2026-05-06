package lphy.base.evolution.substitutionmodel;

import lphy.core.model.GraphicalModelNode;
import lphy.core.model.Value;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.DoubleArray2DValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Abstract base class for empirical amino acid substitution models
 * (LG, BLOSUM62, JTT, Dayhoff, VT, etc.).
 * <p>
 * Subclasses provide an exchangeability matrix and equilibrium frequencies in
 * PAML order ({@code A R N D C Q E G H I L K M F P S T W Y V}). The base class
 * permutes them into alphabetical order ({@code A C D E F G H I K L M N P Q R S T V W Y})
 * to match the encoding used by jebl's {@code AminoAcids} sequence type and BEAST 2.
 * It then forms {@code Q[i][j] = R[i][j] * pi[j]} (with {@code Q[i][i] = -sum_j Q[i][j]})
 * and normalises so that the mean rate is 1 (or {@code meanRate} if supplied).
 *
 * @see WAG
 */
public abstract class EmpiricalAminoAcidModel extends RateMatrix {

    protected final String freqParamName = SubstModelParamNames.FreqParamName;

    /**
     * Maps a PAML-order index ({@code A R N D C Q E G H I L K M F P S T W Y V})
     * to the corresponding alphabetical-order index
     * ({@code A C D E F G H I K L M N P Q R S T V W Y}).
     */
    protected static final int[] PAML_TO_ALPHA = {
            0, 14, 11, 2, 1, 13, 3, 5, 6, 7, 9, 8, 10, 4, 12, 15, 16, 18, 19, 17
    };

    public EmpiricalAminoAcidModel(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical order: A C D E F G H I K L M N P Q R S T V W Y).",
            optional = true) Value<Double[]> freq,
                                   @ParameterInfo(name = RateMatrix.meanRateParamName,
                                           description = "the mean rate of the process. default 1.0",
                                           optional = true) Value<Number> meanRate) {
        super(meanRate);
        if (freq != null) {
            if (freq.value().length != 20)
                throw new IllegalArgumentException("Amino acid frequencies must have 20 dimensions.");
            setParam(freqParamName, freq);
        }
    }

    /**
     * Empirical exchangeability rates in PAML order. Symmetric;
     * the implementation may populate only the lower triangle.
     */
    protected abstract double[][] getEmpiricalRatesPAML();

    /** Empirical equilibrium frequencies in PAML order, summing to 1. */
    protected abstract double[] getEmpiricalFrequenciesPAML();

    public Value<Double[][]> apply() {
        Value<Double[]> freq = getParams().get(freqParamName);
        Double[][] Q = getQ(freq != null ? freq.value() : null);
        return new DoubleArray2DValue(Q, this);
    }

    protected Double[][] getQ(Double[] suppliedFreqsAlpha) {
        // 1. exchangeability matrix in PAML order, symmetrise
        double[][] R = getEmpiricalRatesPAML();
        for (int i = 0; i < 20; i++) {
            for (int j = i + 1; j < 20; j++) {
                if (R[i][j] == 0.0 && R[j][i] != 0.0) R[i][j] = R[j][i];
                else if (R[j][i] == 0.0 && R[i][j] != 0.0) R[j][i] = R[i][j];
            }
        }

        // 2. permute exchangeabilities and empirical frequencies to alphabetical order
        double[] piEmpA = new double[20];
        double[] piPAML = getEmpiricalFrequenciesPAML();
        double[][] Ra = new double[20][20];
        for (int i = 0; i < 20; i++) {
            int ai = PAML_TO_ALPHA[i];
            piEmpA[ai] = piPAML[i];
            for (int j = 0; j < 20; j++) {
                int aj = PAML_TO_ALPHA[j];
                Ra[ai][aj] = R[i][j];
            }
        }

        // 3. choose frequencies (user-supplied alphabetical override or empirical)
        double[] pi = suppliedFreqsAlpha != null
                ? Stream.of(suppliedFreqsAlpha).mapToDouble(Double::doubleValue).toArray()
                : piEmpA;

        // 4. build Q = R * pi with negative row sums on the diagonal
        double[][] Q = new double[20][20];
        for (int i = 0; i < 20; i++) {
            double sum = 0.0;
            for (int j = 0; j < 20; j++) {
                if (i != j) {
                    Q[i][j] = Ra[i][j] * pi[j];
                    sum += Q[i][j];
                }
            }
            Q[i][i] = -sum;
        }

        // 5. normalise to one expected substitution per unit time (or meanRate)
        return normalize(pi, Q, totalRateDefault1());
    }

    public GraphicalModelNode<?> getFreq() {
        return getParams().get(freqParamName);
    }

    /**
     * Holds the parsed contents of a PAML-format AA model {@code .dat} file:
     * exchangeabilities (lower triangle, PAML order) and equilibrium frequencies.
     */
    protected static final class PamlDat {
        public final double[][] R;
        public final double[] pi;
        PamlDat(double[][] R, double[] pi) { this.R = R; this.pi = pi; }
    }

    private static final ConcurrentHashMap<String, PamlDat> DAT_CACHE = new ConcurrentHashMap<>();

    /**
     * Loads a PAML-format AA rate matrix file from the {@code aa-models/} resource
     * directory. Format: 190 lower-triangle exchangeabilities (in 19 increasing-length
     * rows) followed by 20 equilibrium frequencies, both in PAML AA order
     * {@code A R N D C Q E G H I L K M F P S T W Y V}. Trailing comments are ignored.
     * Results are cached, so subclasses can call this from their abstract methods on
     * every invocation cheaply.
     *
     * @param fileName the {@code .dat} file name without extension
     */
    protected static PamlDat loadPamlDat(String fileName) {
        return DAT_CACHE.computeIfAbsent(fileName, EmpiricalAminoAcidModel::parsePamlDat);
    }

    private static PamlDat parsePamlDat(String fileName) {
        String resourcePath = "aa-models/" + fileName + ".dat";
        InputStream in = EmpiricalAminoAcidModel.class.getResourceAsStream(resourcePath);
        if (in == null) throw new IllegalStateException("PAML dat file not found on classpath: " + resourcePath);

        double[] flat = new double[210]; // 190 exchangeabilities + 20 frequencies
        int n = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.US_ASCII))) {
            String line;
            outer:
            while (n < 210 && (line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                for (String tok : trimmed.split("\\s+")) {
                    if (tok.isEmpty()) continue;
                    try {
                        flat[n++] = Double.parseDouble(tok);
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException(
                                "Unexpected non-numeric token '" + tok + "' in " + resourcePath
                                        + " before reading 210 numbers (read " + (n) + ")");
                    }
                    if (n == 210) break outer;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (n != 210)
            throw new IllegalStateException("Expected 210 numbers in " + resourcePath + ", got " + n);

        double[][] R = new double[20][20];
        int k = 0;
        for (int i = 1; i < 20; i++)
            for (int j = 0; j < i; j++)
                R[i][j] = flat[k++];
        double[] pi = new double[20];
        System.arraycopy(flat, k, pi, 0, 20);
        return new PamlDat(R, pi);
    }
}
