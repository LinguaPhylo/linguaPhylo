package lphy.phylospec.convert;

import lphy.core.codebuilder.CanonicalCodeBuilder;
import lphy.core.io.OutputSystem;
import lphy.core.io.UserDir;
import lphy.core.model.Value;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import lphy.core.simulator.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static lphy.phylospec.convert.PhyloSpecToLPhyTest.NORMAL_MODEL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance tests for CLASS_DESIGN3.md §7 step 3: simulating, logging and exporting.
 */
class PhyloSpecToLPhyRunnerTest {

    @TempDir
    Path outDir;

    // FileLoggerListener stores the output directory as a user preference, and the command line sets
    // user.dir to the script's folder, as slphy does: put both back afterwards
    private String savedOutputDir;
    private String savedUserDir;

    @BeforeEach
    void saveDirs() {
        savedOutputDir = OutputSystem.getOrCreateOutputDirectory().getAbsolutePath();
        savedUserDir = UserDir.getUserDir().toString();
    }

    @AfterEach
    void restoreDirs() {
        OutputSystem.setOutputDirectory(savedOutputDir);
        UserDir.setUserDir(savedUserDir);
    }

    private Map<Integer, List<Value>> simulate(String source, long seed, int numReplicates, String prefix) {
        RandomUtils.setSeed(seed);
        REPL dict = PhyloSpecToLPhyRunner.convert(source, prefix);
        return PhyloSpecToLPhyRunner.simulateAndLog(dict, numReplicates, null, outDir.toFile(), prefix);
    }

    private static Map<String, Object> valuesById(List<Value> values) {
        return values.stream().collect(Collectors.toMap(Value::getId, Value::value));
    }

    @Test
    void sameSeedSameValues() {
        Map<String, Object> first = valuesById(simulate(NORMAL_MODEL, 42, 1, "first").get(0));
        Map<String, Object> second = valuesById(simulate(NORMAL_MODEL, 42, 1, "second").get(0));
        assertEquals(Set.of("mu", "sigma", "x"), first.keySet());
        assertEquals(first, second);
    }

    @Test
    void replicatesDifferAndAreLogged() {
        Map<Integer, List<Value>> replicates = simulate(NORMAL_MODEL, 7, 3, "reps");
        assertEquals(3, replicates.size());
        Set<Object> mus = replicates.values().stream()
                .map(values -> valuesById(values).get("mu")).collect(Collectors.toSet());
        assertEquals(3, mus.size(), "each replicate draws new values: " + mus);

        File log = outDir.resolve("reps.log").toFile();
        assertTrue(log.exists(), "FileLoggerListener writes " + log);
    }

    @Test
    void exportedLPhyParsesToTheSameVariables() {
        RandomUtils.setSeed(3);
        REPL dict = PhyloSpecToLPhyRunner.convert(NORMAL_MODEL, "export");
        String code = new CanonicalCodeBuilder().getCode(dict);

        REPL reparsed = new REPL();
        reparsed.parse(code);
        for (String id : List.of("mu", "sigma", "x"))
            assertNotNull(reparsed.getValue(id, GraphicalModel.Context.model), id + " in\n" + code);
    }

    @Test
    void commandLineWritesLogAndLPhy() throws Exception {
        Path script = outDir.resolve("cli.phylospec");
        Files.writeString(script, NORMAL_MODEL);
        int exit = new picocli.CommandLine(new PhyloSpecToLPhyRunner())
                .execute(script.toString(), "-r", "2", "-seed", "11", "-o", outDir.toString());
        assertEquals(0, exit);
        assertTrue(Files.exists(outDir.resolve("cli.log")));
        assertTrue(Files.readString(outDir.resolve("cli.lphy")).contains("Normal("));
    }
}
