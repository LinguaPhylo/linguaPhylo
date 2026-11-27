package lphy.phylospec.convert;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.phylospec.convert.PhyloSpecNames.LPhyGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.phylospec.lexer.TokenType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance tests for CLASS_DESIGN3.md §7 step 1.
 */
class PhyloSpecNamesTest {

    static PhyloSpecNames names;

    @BeforeAll
    static void load() {
        names = PhyloSpecNames.load();
    }

    private static LPhyGenerator onlyGenerator(String phylospecName) {
        List<LPhyGenerator> generators = names.generators(phylospecName);
        assertEquals(1, generators.size(), phylospecName + " -> " + generators);
        return generators.getFirst();
    }

    @Test
    void sameNameNeedsNoAnnotation() {
        LPhyGenerator normal = onlyGenerator("Normal");
        assertEquals("Normal", normal.lphyName());
        assertEquals(PhyloSpecNames.Kind.DISTRIBUTION, normal.kind());
        assertEquals("mean", normal.argumentRename().get("mean"));
        assertEquals("sd", normal.argumentRename().get("sd"));
        normal.argumentRename().forEach((phylospec, lphy) -> assertEquals(phylospec, lphy));
    }

    @Test
    void argumentRenamesComeFromAnnotations() {
        Map<String, String> logNormal = onlyGenerator("LogNormal").argumentRename();
        assertEquals("meanlog", logNormal.get("logMean"));
        assertEquals("sdlog", logNormal.get("logSd"));
        assertNull(logNormal.get("meanlog"), "the LPhy name is not a PhyloSpec argument");

        assertEquals("lambda", onlyGenerator("Yule").argumentRename().get("birthRate"));
        assertEquals("Q", onlyGenerator("PhyloCTMC").argumentRename().get("qMatrix"));
    }

    @Test
    void generatorRenamesComeFromAnnotations() {
        assertEquals("jukesCantor", onlyGenerator("jc69").lphyName());
        assertTrue(names.generators("jukesCantor").isEmpty(), "the LPhy name is not a PhyloSpec name");

        LPhyGenerator discreteGamma = onlyGenerator("DiscreteGamma");
        assertEquals("DiscretizeGamma", discreteGamma.lphyName());
        assertEquals("ncat", discreteGamma.argumentRename().get("numCategories"));
    }

    @Test
    void createTaxaIsUnderTaxa() {
        LPhyGenerator taxa = onlyGenerator("taxa");
        assertEquals(PhyloSpecNames.Kind.FUNCTION, taxa.kind());
        assertEquals("names", taxa.argumentRename().get("names"));
        assertEquals("ages", taxa.argumentRename().get("ages"));
    }

    @Test
    void handMaintainedTables() {
        assertNotNull(names.mathFunction("sqrt"));
        assertNull(names.mathFunction("not"), "! is an operator, not a math function");
        assertNotNull(names.binaryOperator(TokenType.PLUS));
        assertNotNull(names.binaryOperator(TokenType.BANG_EQUAL));
        assertNotNull(names.unaryOperator(TokenType.BANG));
        assertNull(names.unaryOperator(TokenType.MINUS), "LPhy has no unary minus");

        assertEquals("tree", names.methodCall("rootAge").receiverArgument());
        assertEquals("branchCount", names.methodCall("numBranches").lphyMethod());

        PhyloSpecNames.IIDShorthand gammaInv = names.iidShorthand("DiscreteGammaInv");
        assertEquals("DiscreteGamma", gammaInv.baseName());
        assertEquals("numSites", gammaInv.replicatesArgument());
        assertTrue(names.generators("taxon").isEmpty());
    }

    @Test
    void twoPhyloSpecNamesForOneLPhyNameFail() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
                PhyloSpecNames.load(List.of(), List.of(ConflictA.class, ConflictB.class)));
        assertTrue(e.getMessage().contains("conflictTest"), e.getMessage());
    }

    @Test
    void oneArgumentRenamedTwoWaysFails() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
                PhyloSpecNames.load(List.of(), List.of(RenameConflict.class)));
        assertTrue(e.getMessage().contains("'x'"), e.getMessage());
    }

    //*** test-only generators ***//

    public static class ConflictA extends DeterministicFunction<Double> {
        public ConflictA(@ParameterInfo(name = "a", description = "a") Value<Double> a) {
            setParam("a", a);
        }

        @GeneratorInfo(name = "conflictTest", phylospec = "A", description = "test only")
        public Value<Double> apply() {
            return null;
        }
    }

    public static class ConflictB extends DeterministicFunction<Double> {
        public ConflictB(@ParameterInfo(name = "a", description = "a") Value<Double> a) {
            setParam("a", a);
        }

        @GeneratorInfo(name = "conflictTest", phylospec = "B", description = "test only")
        public Value<Double> apply() {
            return null;
        }
    }

    public static class RenameConflict extends DeterministicFunction<Double> {
        public RenameConflict(@ParameterInfo(name = "a", phylospec = "x", description = "a") Value<Double> a) {
            setParam("a", a);
        }

        public RenameConflict(@ParameterInfo(name = "b", phylospec = "x", description = "b") Value<Double> b,
                              @ParameterInfo(name = "c", description = "c") Value<Double> c) {
            setParam("b", b);
        }

        @GeneratorInfo(name = "renameConflictTest", description = "test only")
        public Value<Double> apply() {
            return null;
        }
    }
}
