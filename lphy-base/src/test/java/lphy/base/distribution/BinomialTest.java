package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.parser.REPL;
import lphy.core.simulator.RandomUtils;
import lphy.core.simulator.Sampler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BinomialTest {

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(123);
    }

    // #579: setParam used to find setP/setN(Value) by reflection and fail on the raw value
    @Test
    public void testSetParam() {
        Binomial binomial = new Binomial(new Value<>("p", 0.3), new Value<>("n", 10));
        assertDoesNotThrow(() -> binomial.setParam(DistributionConstants.pParamName, new Value<>("p", 0.9)));
        assertDoesNotThrow(() -> binomial.setParam(DistributionConstants.nParamName, new Value<>("n", 1000)));
        // the distribution is rebuilt from the new parameters: E(x) = 900, sd = ~9.5
        int x = binomial.sample().value();
        assertTrue(x > 800 && x < 1000, "x = " + x);
    }

    @Test
    public void testParseAndResample() {
        REPL repl = new REPL();
        repl.parse("x ~ Binomial(p=0.3, n=10);");
        Integer x = (Integer) repl.getModelDictionary().get("x").value();
        assertTrue(x >= 0 && x <= 10, "x = " + x);

        Sampler sampler = new Sampler(repl);
        for (int i = 0; i < 5; i++) {
            sampler.sample(null);
            x = (Integer) repl.getModelDictionary().get("x").value();
            assertTrue(x >= 0 && x <= 10, "x = " + x);
        }
    }
}
