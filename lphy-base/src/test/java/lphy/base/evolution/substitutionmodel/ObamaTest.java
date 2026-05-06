package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Obama is a dispatcher: for every model index it must produce the
 * same Q matrix as instantiating that model directly. Also covers the
 * {@code useExternalFreqs} switch and the {@code models} subset selection.
 */
class ObamaTest {

    private static final EmpiricalAminoAcidModel[] MODELS = {
            new WAG(null, null),    new JTT(null, null),    new LG(null, null),
            new Dayhoff(null, null), new DCMut(null, null), new CpREV(null, null),
            new MtREV(null, null),  new MtMam(null, null),  new MtArt(null, null),
            new Blosum62(null, null), new VT(null, null),   new RtREV(null, null),
            new FLU(null, null),    new HIVb(null, null),   new HIVw(null, null)
    };

    private static Value<Double[]> uniformFreqs() {
        Double[] u = new Double[20];
        for (int i = 0; i < 20; i++) u[i] = 0.05;
        return new Value<>("freq", u);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14})
    void dispatchMatchesDirectInstantiation(int idx) {
        Obama obama = new Obama(new Value<>("modelIndicator", idx), null, null, null, null);
        Double[][] Qo = obama.apply().value();
        Double[][] Qd = MODELS[idx].apply().value();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                assertEquals(Qd[i][j], Qo[i][j], 1e-15,
                        "Obama(" + idx + ") differs from " + Obama.ALL_MODELS[idx]
                                + " at (" + i + "," + j + ")");
            }
        }
    }

    @Test
    void rejectsOutOfRangeIndicator() {
        Obama o = new Obama(new Value<>("modelIndicator", 15), null, null, null, null);
        assertThrows(IllegalArgumentException.class, o::apply);
    }

    @Test
    void unsuppliedFreqsUseEmpirical() {
        Obama obama = new Obama(new Value<>("modelIndicator", 2), null, null, null, null);
        Double[][] Q = obama.apply().value();
        Double[][] Qexp = new LG(null, null).apply().value();
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 20; j++)
                assertEquals(Qexp[i][j], Q[i][j], 1e-15);
    }

    @Test
    void freqOverridesEmpiricalWhenUseExternalFreqsUnset() {
        Obama obama = new Obama(new Value<>("modelIndicator", 2),
                null, null, uniformFreqs(), null);
        Double[][] Q = obama.apply().value();
        for (int i = 0; i < 20; i++)
            for (int j = i + 1; j < 20; j++)
                assertEquals(Q[i][j], Q[j][i], 1e-12);
    }

    @Test
    void useExternalFreqsTrueUsesSuppliedFreq() {
        Obama obama = new Obama(new Value<>("modelIndicator", 2),
                null, new Value<>("useExternalFreqs", true), uniformFreqs(), null);
        Double[][] Q = obama.apply().value();
        for (int i = 0; i < 20; i++)
            for (int j = i + 1; j < 20; j++)
                assertEquals(Q[i][j], Q[j][i], 1e-12);
    }

    @Test
    void useExternalFreqsFalseIgnoresSuppliedFreq() {
        Obama obama = new Obama(new Value<>("modelIndicator", 2),
                null, new Value<>("useExternalFreqs", false), uniformFreqs(), null);
        Double[][] Q = obama.apply().value();
        Double[][] Qexp = new LG(null, null).apply().value();
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 20; j++)
                assertEquals(Qexp[i][j], Q[i][j], 1e-15);
    }

    @Test
    void useExternalFreqsTrueWithoutFreqIsRejected() {
        Obama o = new Obama(new Value<>("modelIndicator", 2),
                null, new Value<>("useExternalFreqs", true), null, null);
        assertThrows(IllegalArgumentException.class, o::apply);
    }

    @Test
    void modelsSubsetRedirectsIndexing() {
        // index 0 should give LG (not WAG) when models = ["LG", "JTT", "Blosum62"]
        Obama obama = new Obama(new Value<>("modelIndicator", 0),
                new Value<>("models", new String[]{"LG", "JTT", "Blosum62"}),
                null, null, null);
        Double[][] Q = obama.apply().value();
        Double[][] Qlg = new LG(null, null).apply().value();
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 20; j++)
                assertEquals(Qlg[i][j], Q[i][j], 1e-15);

        Obama obama2 = new Obama(new Value<>("modelIndicator", 2),
                new Value<>("models", new String[]{"LG", "JTT", "Blosum62"}),
                null, null, null);
        Double[][] Q2 = obama2.apply().value();
        Double[][] Qb62 = new Blosum62(null, null).apply().value();
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 20; j++)
                assertEquals(Qb62[i][j], Q2[i][j], 1e-15);
    }

    @Test
    void modelsSubsetIndicatorRangeIsTighter() {
        // models has 3 entries, so indicator=3 must be rejected
        Obama o = new Obama(new Value<>("modelIndicator", 3),
                new Value<>("models", new String[]{"LG", "JTT", "Blosum62"}),
                null, null, null);
        assertThrows(IllegalArgumentException.class, o::apply);
    }

    @Test
    void modelNamesAreCaseInsensitive() {
        Obama obama = new Obama(new Value<>("modelIndicator", 1),
                new Value<>("models", new String[]{"lg", "jtt", "BLOSUM62"}),
                null, null, null);
        Double[][] Q = obama.apply().value();
        Double[][] Qjtt = new JTT(null, null).apply().value();
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 20; j++)
                assertEquals(Qjtt[i][j], Q[i][j], 1e-15);
    }

    @Test
    void unknownModelNameIsRejectedAtConstruction() {
        assertThrows(IllegalArgumentException.class,
                () -> new Obama(new Value<>("modelIndicator", 0),
                        new Value<>("models", new String[]{"LG", "BogusModel"}),
                        null, null, null));
    }

    @Test
    void emptyModelsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Obama(new Value<>("modelIndicator", 0),
                        new Value<>("models", new String[0]),
                        null, null, null));
    }
}
