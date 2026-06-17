package lphy.base.evolution.birthdeath;

import lphy.core.model.Generator;
import lphy.core.model.Value;
import lphy.core.parser.ParserUtils;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies the step-4 "full fold": several distinct generator classes share one user-facing name and
 * are dispatched purely by which arguments are supplied. Also checks that the deprecated old names
 * still resolve via the alias infrastructure. Uses the matcher directly (no sampling) so each case
 * asserts exactly one match and the precise class chosen.
 */
public class FoldedNameDispatchTest {

    private static Value<?> v(Object x) {
        return new Value<>(null, x);
    }

    /** @param kv alternating argument name (String) and value (Object) */
    private static Class<?> resolve(String name, Object... kv) {
        Map<String, Value> args = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) args.put((String) kv[i], v(kv[i + 1]));
        List<Generator> matches = ParserUtils.getMatchingGenerativeDistributions(name, args);
        assertEquals(1, matches.size(),
                name + " with " + args.keySet() + " should resolve to exactly one generator, got " + matches);
        return matches.get(0).getClass();
    }

    @Test
    void birthDeathFoldDispatch() {
        assertSame(BirthDeathTree.class,
                resolve("BirthDeath", "lambda", 1.0, "mu", 0.5, "n", 10, "rootAge", 4.0));
        assertSame(BirthDeathSamplingTree.class,
                resolve("BirthDeath", "lambda", 1.0, "mu", 0.5, "rho", 0.5, "rootAge", 4.0));
        assertSame(BirthDeathTreeDT.class,
                resolve("BirthDeath", "diversification", 0.5, "turnover", 0.5, "rootAge", 4.0));
        assertSame(BirthDeathSamplingTreeDT.class,
                resolve("BirthDeath", "diversification", 0.5, "turnover", 0.5, "rho", 0.5, "rootAge", 4.0));
    }

    @Test
    void fossilBirthDeathFoldDispatch() {
        assertSame(FossilBirthDeathTree.class,
                resolve("FossilBirthDeath", "lambda", 1.0, "mu", 0.5, "rho", 1.0, "psi", 0.5, "n", 10));
        assertSame(SimFBDAge.class,
                resolve("FossilBirthDeath", "lambda", 1.0, "mu", 0.5, "rho", 0.5, "psi", 0.5, "originAge", 4.0));
        assertSame(FossilBirthDeathTreeDT.class,
                resolve("FossilBirthDeath", "diversification", 0.5, "turnover", 0.5, "rho", 1.0, "samplingProportion", 0.5, "n", 10));
        assertSame(SimFBDAgeDT.class,
                resolve("FossilBirthDeath", "diversification", 0.5, "turnover", 0.5, "rho", 0.5, "samplingProportion", 0.5, "originAge", 4.0));
    }

    @Test
    void deprecatedAliasesStillResolve() {
        assertSame(BirthDeathSamplingTree.class,
                resolve("BirthDeathSampling", "lambda", 1.0, "mu", 0.5, "rho", 0.5, "rootAge", 4.0));
        assertSame(BirthDeathSerialSamplingTree.class,
                resolve("BirthDeathSerialSampling", "lambda", 1.0, "mu", 0.5, "rho", 0.1, "psi", 1.0, "rootAge", 4.0, "n", 10));
        assertSame(FossilBirthDeathTree.class,
                resolve("FossilBirthDeathTree", "lambda", 1.0, "mu", 0.5, "rho", 1.0, "psi", 0.5, "n", 10));
        assertSame(SimFBDAge.class,
                resolve("SimFBDAge", "lambda", 1.0, "mu", 0.5, "rho", 0.5, "psi", 0.5, "originAge", 4.0));
        // FBD shorthand alias for the fossil family
        assertSame(SimFBDAge.class,
                resolve("FBD", "lambda", 1.0, "mu", 0.5, "rho", 0.5, "psi", 0.5, "originAge", 4.0));
    }

    @Test
    void serialRename() {
        assertSame(BirthDeathSerialSamplingTree.class,
                resolve("BirthDeathSerial", "lambda", 1.0, "mu", 0.5, "rho", 0.1, "psi", 1.0, "rootAge", 4.0, "n", 10));
    }
}
