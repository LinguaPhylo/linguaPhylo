package lphy.base.distribution;

import lphy.core.parser.REPL;
import lphy.core.simulator.RandomUtils;
import lphy.core.simulator.Sampler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parses and resamples distributions whose setParam used to fail: their public setX(Value) setters were
 * found by Generator.setParam's reflection and invoked with the raw value (#579). Resampling also prints
 * the model from getParams() and parses it again, which catches a getParams() key that is not an argument.
 */
public class DiscreteSetParamTest {

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(123);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "x ~ Binomial(p=0.3, n=10);",
            "x ~ Geometric(p=0.3);",
            "x ~ NegativeBinomial(r=5, p=0.3);"
    })
    public void testParseAndResample(String lphy) {
        REPL repl = new REPL();
        repl.parse(lphy);
        assertTrue((Integer) repl.getModelDictionary().get("x").value() >= 0);

        Sampler sampler = new Sampler(repl);
        for (int i = 0; i < 5; i++) {
            sampler.sample(null);
            assertTrue((Integer) repl.getModelDictionary().get("x").value() >= 0, lphy);
        }
    }
}
