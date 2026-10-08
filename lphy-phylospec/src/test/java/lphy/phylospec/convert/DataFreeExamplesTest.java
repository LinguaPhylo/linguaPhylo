package lphy.phylospec.convert;

import lphy.base.distribution.DiscretizedGamma;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.alignment.Alignment;
import lphy.core.codebuilder.CanonicalCodeBuilder;
import lphy.core.io.OutputSystem;
import lphy.core.model.Value;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import lphy.core.simulator.RandomUtils;
import lphy.core.vectorization.IID;
import lphy.phylospec.convert.PhyloSpecToLPhyRunner.ConversionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance tests for CLASS_DESIGN3.md §7 step 4: arrays, taxa and IID shorthands, driven by the
 * two data-free examples in lphy-phylospec/examples.
 */
class DataFreeExamplesTest {

    // surefire runs from the module folder
    static final Path EXAMPLES = Path.of("examples");

    @TempDir
    Path outDir;

    private String savedOutputDir;

    @BeforeEach
    void saveOutputDir() {
        savedOutputDir = OutputSystem.getOrCreateOutputDirectory().getAbsolutePath();
    }

    @AfterEach
    void restoreOutputDir() {
        OutputSystem.setOutputDirectory(savedOutputDir);
    }

    private static Value<?> model(REPL dict, String id) {
        return dict.getValue(id, GraphicalModel.Context.model);
    }

    /** Converts, simulates and logs the example, and checks its .lphy export re-parses. */
    private REPL runExample(String fileName) throws IOException {
        String prefix = fileName.replace(PhyloSpecToLPhyRunner.PHYLOSPEC_EXTENSION, "");
        RandomUtils.setSeed(123);
        REPL dict = PhyloSpecToLPhyRunner.convert(Files.readString(EXAMPLES.resolve(fileName)), prefix);

        Map<Integer, List<Value>> replicates =
                PhyloSpecToLPhyRunner.simulateAndLog(dict, 2, null, outDir.toFile(), prefix);
        assertEquals(2, replicates.size());
        // FileLoggerListener writes each replicate's alignment to its own file
        try (var files = Files.list(outDir)) {
            List<String> names = files.map(f -> f.getFileName().toString()).toList();
            for (int i = 0; i < 2; i++) {
                String alignmentFile = prefix + "_r" + i + "_sim";
                assertTrue(names.stream().anyMatch(n -> n.startsWith(alignmentFile)), alignmentFile + " in " + names);
            }
        }

        String code = new CanonicalCodeBuilder().getCode(dict);
        REPL reparsed = new REPL();
        reparsed.parse(code);
        for (String id : List.of("tree", "q", "rates", "sim"))
            assertNotNull(model(reparsed, id), id + " after re-parsing:\n" + code);
        return dict;
    }

    private static Alignment alignment(REPL dict) {
        return assertInstanceOf(Alignment.class, model(dict, "sim").value());
    }

    @Test
    void literalSiteRates() throws IOException {
        REPL dict = runExample("simLiteralSiteRates.phylospec");

        Alignment sim = alignment(dict);
        assertEquals(4, sim.ntaxa());
        assertEquals(5, sim.nchar());
        // the alignment lists its taxa in the tree's leaf order
        assertEquals(java.util.Set.of("a", "b", "c", "d"), java.util.Set.of(sim.getTaxaNames()));

        Value<?> rates = model(dict, "rates");
        assertArrayEquals(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0}, (Double[]) rates.value());
        assertTrue(rates.isConstant(), "an array of literals is one constant");
    }

    @Test
    void discreteGammaSites() throws IOException {
        REPL dict = runExample("simDiscreteGammaSites.phylospec");

        Alignment sim = alignment(dict);
        assertEquals(4, sim.ntaxa());
        assertEquals(200, sim.nchar());

        Value<?> rates = model(dict, "rates");
        assertEquals(200, ((Double[]) rates.value()).length);
        IID<?> iid = assertInstanceOf(IID.class, rates.getGenerator());
        assertInstanceOf(DiscretizedGamma.class, iid.getBaseDistribution());
    }

    @Test
    void taxonArrayKeepsSpeciesAndAges() {
        REPL dict = PhyloSpecToLPhyRunner.convert("""
                Taxa taxa = [taxon(name="a", species="x", age=1.5), taxon(name="b", species="y")]
                """, "taxa");
        Taxa taxa = assertInstanceOf(Taxa.class, model(dict, "taxa").value());
        assertArrayEquals(new String[]{"a", "b"}, taxa.getTaxaNames());
        assertArrayEquals(new String[]{"x", "y"}, taxa.getSpecies());
        assertEquals(List.of(1.5, 0.0), Arrays.asList(taxa.getAges()));
    }

    //*** rejected forms ***//

    private static String rejected(String source) {
        ConversionException e = assertThrows(ConversionException.class,
                () -> PhyloSpecToLPhyRunner.convert(source, "rejected"));
        return e.getMessage();
    }

    @Test
    void invariantSitesAreRejected() {
        String message = rejected("""
                Vector<Rate> rates ~ DiscreteGammaInv(shape=0.5, numCategories=4, invariantProportion=0.1, numSites=10)
                """);
        assertTrue(message.contains("Not supported by LPhy yet"), message);
        assertTrue(message.contains("invariantProportion"), message);
    }

    @Test
    void zeroInvariantSitesAreAccepted() {
        REPL dict = PhyloSpecToLPhyRunner.convert("""
                Vector<Rate> rates ~ DiscreteGammaInv(shape=0.5, numCategories=4, invariantProportion=0, numSites=10)
                """, "zeroInvariant");
        assertEquals(10, ((Double[]) model(dict, "rates").value()).length);
    }

    @Test
    void taxonOutsideAnArrayIsRejected() {
        String message = rejected("Taxon t = taxon(name=\"a\")\n");
        assertTrue(message.contains("Not supported by LPhy yet"), message);
    }

    @Test
    void speciesForSomeTaxaIsRejected() {
        String message = rejected("""
                Taxa taxa = [taxon(name="a", species="x"), taxon(name="b")]
                """);
        assertTrue(message.contains("species for every taxon"), message);
    }
}
