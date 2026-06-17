package lphy.core.parser.argument;

import lphy.core.model.Value;
import lphy.core.model.annotation.ParameterInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for parameter alias resolution via {@link Argument#matchingKey(Set)}, the core of the
 * backward-compatible parameter-renaming infrastructure ({@link ParameterInfo#aliases()}).
 */
public class ArgumentAliasTest {

    // a constructor annotated as a generator constructor would be; "rho" has deprecated aliases
    static class Dummy {
        public Dummy(
                @ParameterInfo(name = "rho", aliases = {"frac", "p"}, description = "sampling") Value<Number> rho,
                @ParameterInfo(name = "lambda", description = "birth rate") Value<Number> lambda) {
        }
    }

    private Argument arg(String name) {
        List<Argument> args = ArgumentUtils.getArguments(Dummy.class.getConstructors()[0]);
        return args.stream().filter(a -> a.name.equals(name)).findFirst().orElseThrow();
    }

    @Test
    void canonicalNameIsPreferred() {
        Argument rho = arg("rho");
        assertEquals("rho", rho.matchingKey(Set.of("rho", "lambda")));
        // even when an alias is also (wrongly) present, the canonical wins
        assertEquals("rho", rho.matchingKey(Set.of("rho", "frac")));
    }

    @Test
    void aliasesAreMatched() {
        Argument rho = arg("rho");
        assertEquals("frac", rho.matchingKey(Set.of("frac", "lambda")));
        assertEquals("p", rho.matchingKey(Set.of("p", "lambda")));
    }

    @Test
    void absentArgumentReturnsNull() {
        assertNull(arg("rho").matchingKey(Set.of("lambda")));
    }

    @Test
    void argumentWithoutAliasesOnlyMatchesItsName() {
        Argument lambda = arg("lambda");
        assertEquals("lambda", lambda.matchingKey(Set.of("lambda")));
        assertNull(lambda.matchingKey(Set.of("frac")));
    }
}
