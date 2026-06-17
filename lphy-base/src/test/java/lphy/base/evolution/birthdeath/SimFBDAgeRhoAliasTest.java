package lphy.base.evolution.birthdeath;

import lphy.core.model.Value;
import lphy.core.parser.ParserSingleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test for the {@code frac} -> {@code rho} parameter rename of SimFBDAge / SimFBDAgeDT.
 * The canonical name is {@code rho}; {@code frac} is retained as a deprecated alias for backward
 * compatibility. Exercises the parameter-alias infrastructure end-to-end.
 */
public class SimFBDAgeRhoAliasTest {

    @Test
    void canonicalRhoWorks() {
        Object o = ParserSingleton.parse("tree ~ SimFBDAge(lambda=1.0, mu=0.5, rho=0.5, psi=0.5, originAge=4.0);");
        SimFBDAge gen = (SimFBDAge) ((Value) o).getGenerator();
        assertNotNull(gen.getParams().get("rho"), "value must be wired under canonical 'rho'");
        assertNull(gen.getParams().get("frac"), "nothing should be wired under the alias 'frac'");
    }

    @Test
    void fracAliasStillResolvesToRho() {
        Object o = ParserSingleton.parse("tree ~ SimFBDAge(lambda=1.0, mu=0.5, frac=0.5, psi=0.5, originAge=4.0);");
        SimFBDAge gen = (SimFBDAge) ((Value) o).getGenerator();
        // the deprecated alias must be canonicalised: the value is wired under 'rho', not 'frac'
        assertNotNull(gen.getParams().get("rho"), "alias 'frac' must wire the value under canonical 'rho'");
        assertNull(gen.getParams().get("frac"));
        assertEquals(0.5, ((Number) gen.getRho().value()).doubleValue());
    }

    @Test
    void fracAliasWorksForDiversificationTurnover() {
        Object o = ParserSingleton.parse(
                "tree ~ SimFBDAge(diversification=0.5, turnover=0.5, frac=1.0, samplingProportion=0.5, originAge=4.0);");
        SimFBDAgeDT gen = (SimFBDAgeDT) ((Value) o).getGenerator();
        assertNotNull(gen.getParams().get("rho"), "alias 'frac' must wire the value under canonical 'rho'");
        assertNull(gen.getParams().get("frac"));
    }
}
