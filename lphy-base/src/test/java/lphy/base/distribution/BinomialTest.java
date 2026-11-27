package lphy.base.distribution;

import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
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
}
