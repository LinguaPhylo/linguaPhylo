package lphy.phylospec.convert;

import lphy.core.codebuilder.CanonicalCodeBuilder;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import lphy.core.simulator.RandomUtils;
import lphy.core.vectorization.IID;
import lphy.core.vectorization.VectorizedRandomVariable;
import lphy.phylospec.convert.PhyloSpecToLPhyRunner.ConversionException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance tests for CLASS_DESIGN3.md §7 step 5: observed as, IID, operators, math functions and
 * method calls. Each script is also round-tripped through .lphy export.
 */
class ConverterFeaturesTest {

    private static Value<?> model(REPL dict, String id) {
        return dict.getValue(id, GraphicalModel.Context.model);
    }

    private static Value<?> data(REPL dict, String id) {
        return dict.getValue(id, GraphicalModel.Context.data);
    }

    /** Converts the script and checks that its .lphy export re-parses with the given variables. */
    private static REPL convertAndRoundTrip(String source, String... ids) {
        RandomUtils.setSeed(5);
        REPL dict = PhyloSpecToLPhyRunner.convert(source, "features");
        String code = new CanonicalCodeBuilder().getCode(dict);
        REPL reparsed = new REPL();
        reparsed.parse(code);
        for (String id : ids)
            assertNotNull(model(reparsed, id), id + " after re-parsing:\n" + code);
        return dict;
    }

    @Test
    void observedScalarIsClamped() {
        REPL dict = convertAndRoundTrip("""
                Real mu ~ Normal(mean=0.0, sd=1.0)
                Real x ~ Normal(mean=mu, sd=1.0) observed as 1.5
                """, "mu", "x");
        RandomVariable<?> x = assertInstanceOf(RandomVariable.class, model(dict, "x"));
        assertTrue(x.isObserved());
        assertEquals(1.5, x.value());
        assertEquals(1.5, data(dict, "x").value());
        assertTrue(dict.isObserved("x"));
    }

    @Test
    void observedIIDArrayClampsEveryComponent() {
        REPL dict = convertAndRoundTrip("""
                Vector<Real> xs ~ IID(base=Normal(mean=0.0, sd=1.0), num=3) observed as [1.0, 2.0, 3.0]
                """, "xs");
        VectorizedRandomVariable<?> xs = assertInstanceOf(VectorizedRandomVariable.class, model(dict, "xs"));
        assertArrayEquals(new Double[]{1.0, 2.0, 3.0}, (Double[]) xs.value());
        assertTrue(dict.isObserved("xs"));
    }

    @Test
    void observingAVariableIsRejected() {
        ConversionException e = assertThrows(ConversionException.class, () -> PhyloSpecToLPhyRunner.convert("""
                Real obs = 1.5
                Real x ~ Normal(mean=0.0, sd=1.0) observed as obs
                """, "observeVariable"));
        assertTrue(e.getMessage().contains("Observe an expression, not a variable"), e.getMessage());
    }

    @Test
    void iidBecomesReplicates() {
        REPL dict = convertAndRoundTrip("""
                Real mu ~ Normal(mean=0.0, sd=1.0)
                Vector<Real> xs ~ IID(base=Normal(mean=mu, sd=1.0), num=4)
                """, "mu", "xs");
        Value<?> xs = model(dict, "xs");
        assertEquals(4, ((Double[]) xs.value()).length);
        IID<?> iid = assertInstanceOf(IID.class, xs.getGenerator());
        assertSame(model(dict, "mu"), iid.getBaseDistribution().getParams().get("mean"));
    }

    @Test
    void operatorsAndMathFunctions() {
        REPL dict = convertAndRoundTrip("""
                PositiveReal s ~ LogNormal(logMean=0.0, logSd=0.5)
                Real y = sqrt(s) + 1.0
                Boolean big = s > 1.0
                """, "s", "y", "big");
        double s = (Double) model(dict, "s").value();
        assertEquals(Math.sqrt(s) + 1.0, ((Number) model(dict, "y").value()).doubleValue(), 1e-12);
        assertEquals(s > 1.0, model(dict, "big").value());
    }

    @Test
    void unaryMinusIsRejected() {
        ConversionException e = assertThrows(ConversionException.class, () -> PhyloSpecToLPhyRunner.convert("""
                Real s ~ Normal(mean=0.0, sd=1.0)
                Real y = -s
                """, "unaryMinus"));
        assertTrue(e.getMessage().contains("Not supported by LPhy yet"), e.getMessage());
    }

    @Test
    void methodCalls() {
        REPL dict = convertAndRoundTrip("""
                Tree tree ~ Yule(birthRate=1.0, taxa=[taxon(name="a"), taxon(name="b"), taxon(name="c")])
                Age height = rootAge(tree)
                PositiveInteger branches = numBranches(tree)
                """, "tree", "height", "branches");
        assertTrue(((Number) model(dict, "height").value()).doubleValue() > 0);
        assertEquals(4, model(dict, "branches").value(), "a rooted tree of 3 taxa has 4 branches");
        assertEquals(List.of("tree"), List.of(model(dict, "tree").getId()));
    }
}
