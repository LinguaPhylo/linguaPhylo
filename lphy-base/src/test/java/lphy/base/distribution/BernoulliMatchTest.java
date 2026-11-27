package lphy.base.distribution;

import lphy.core.model.Generator;
import lphy.core.model.Value;
import lphy.core.parser.ParserUtils;
import lphy.core.parser.REPL;
import lphy.core.simulator.RandomUtils;
import lphy.core.spi.LoaderManager;
import lphy.core.vectorization.IID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bernoulli(p, replicates) matches both BernoulliMulti directly and Bernoulli wrapped in IID.
 * The parser used to pick whichever came first in a HashSet, so the choice was arbitrary.
 */
public class BernoulliMatchTest {

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(1);
    }

    private static Generator generatorOf(String lphy) {
        REPL repl = new REPL();
        repl.parse(lphy);
        return repl.getModelDictionary().get("x").getGenerator();
    }

    @Test
    public void testReplicatesPicksBernoulliMulti() {
        Generator generator = generatorOf("x ~ Bernoulli(p=0.5, replicates=3);");
        assertInstanceOf(BernoulliMulti.class, generator);

        Map<String, Value> arguments = new LinkedHashMap<>();
        arguments.put("p", new Value<>(null, 0.5));
        arguments.put("replicates", new Value<>(null, 3));
        List<Generator> matches = ParserUtils.getMatchingGenerativeDistributions("Bernoulli", arguments);
        assertEquals(1, matches.size(), "matches: " + matches);
        assertInstanceOf(BernoulliMulti.class, matches.getFirst());
    }

    @Test
    public void testOtherCallsAreUnchanged() {
        assertInstanceOf(Bernoulli.class, generatorOf("x ~ Bernoulli(p=0.5);"));
        assertInstanceOf(BernoulliMulti.class, generatorOf("x ~ Bernoulli(p=0.5, replicates=4, minSuccesses=2);"));
        // a distribution without its own replicates argument is still vectorised by IID
        assertInstanceOf(IID.class, generatorOf("x ~ Normal(mean=0.0, sd=1.0, replicates=4);"));
    }

    @Test
    public void testGeneratorsKeepRegistrationOrder() {
        List<Class<?>> classes = new ArrayList<>(LoaderManager.getAllGenerativeDistributionClasses("Bernoulli"));
        assertEquals(List.of(Bernoulli.class, BernoulliMulti.class), classes);
    }
}
