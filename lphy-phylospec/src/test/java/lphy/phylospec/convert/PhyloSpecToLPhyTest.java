package lphy.phylospec.convert;

import lphy.base.distribution.Normal;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import lphy.core.simulator.RandomUtils;
import lphy.phylospec.convert.PhyloSpecToLPhyRunner.ConversionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance tests for CLASS_DESIGN3.md §7 step 2: the converter core, §3.1–§3.6 without IID.
 */
class PhyloSpecToLPhyTest {

    static final String NORMAL_MODEL = """
            Real mu ~ Normal(mean=0.0, sd=1.0)
            PositiveReal sigma ~ LogNormal(logMean=0.0, logSd=0.5)
            Real x ~ Normal(mean=mu, sd=sigma)
            """;

    static Value<?> model(REPL dict, String id) {
        return dict.getValue(id, GraphicalModel.Context.model);
    }

    @Test
    void drawsBecomeWiredRandomVariables() {
        RandomUtils.setSeed(1);
        REPL dict = PhyloSpecToLPhyRunner.convert(NORMAL_MODEL, "normal");

        Value<?> mu = model(dict, "mu");
        Value<?> sigma = model(dict, "sigma");
        Value<?> x = model(dict, "x");
        assertInstanceOf(RandomVariable.class, mu);
        assertInstanceOf(RandomVariable.class, sigma);
        assertInstanceOf(RandomVariable.class, x);
        assertTrue((Double) sigma.value() > 0);

        Normal normal = assertInstanceOf(Normal.class, x.getGenerator());
        assertSame(mu, normal.getParams().get("mean"));
        assertSame(sigma, normal.getParams().get("sd"));
        assertTrue(mu.getOutputs().contains(normal), "setInput must register the generator as an output");
    }

    @Test
    void assignmentNamesTheValue() {
        REPL dict = PhyloSpecToLPhyRunner.convert("""
                Real mu ~ Normal(mean=0.0, sd=1.0)
                Real twice = 2.0 * mu
                """, "assign");
        Value<?> twice = model(dict, "twice");
        assertEquals("twice", twice.getId());
        assertEquals(2.0 * (Double) model(dict, "mu").value(), ((Number) twice.value()).doubleValue(), 1e-12);
    }

    @Test
    void aliasingIsRejected() {
        ConversionException e = assertThrows(ConversionException.class, () -> PhyloSpecToLPhyRunner.convert("""
                Real mu ~ Normal(mean=0.0, sd=1.0)
                Real y = mu
                """, "alias"));
        assertTrue(e.getMessage().contains("Aliasing"), e.getMessage());
        assertTrue(e.getMessage().contains("line 2"), "the error points at the statement: " + e.getMessage());
    }

    @Test
    void typeErrorsAreReported() {
        ConversionException e = assertThrows(ConversionException.class, () -> PhyloSpecToLPhyRunner.convert(
                "Real x ~ Normal(mean=undefinedVariable, sd=1.0)\n", "typeError"));
        assertTrue(e.getMessage().contains("undefinedVariable"), e.getMessage());
    }
}
