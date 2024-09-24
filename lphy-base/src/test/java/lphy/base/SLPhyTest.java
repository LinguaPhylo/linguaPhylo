package lphy.base;

import lphy.core.simulator.SLPhy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;


public class SLPhyTest {
    Path exampleDir;

    CommandLine cmd;
//    StringWriter sw;

    @BeforeEach
    void setUp() {
        SLPhy slPhy = new SLPhy();
        cmd = new CommandLine(slPhy);
//        sw = new StringWriter();
//        cmd.setOut(new PrintWriter(sw));

        final String wd = System.getProperty("user.dir");
        // github workflow run tests in $project_root/lphy, so "**/lphy/../examples" is required
        exampleDir = Paths.get(wd, "..", "examples");

    }

    @Test
    void testMacro() {
        Path macroDir = Paths.get(exampleDir.toAbsolutePath().toString(), "macro");
        Path macroLPhy = Paths.get(macroDir.toString(), "MacroLanguage.lphy");
        assertTrue(macroLPhy.toFile().exists(), "Cannot find LPhy file : " + macroLPhy);

        // MacroLanguage.lphy  :  L = {{L = 100}}; taxa = taxa(names=1:{{n = 10}});
        int exitCode = cmd.execute("-D", "\"n=5;L=50\"", macroLPhy.toString());
        assertEquals(0, exitCode);

//        String outStr = sw.toString();

        // Create file : MacroLanguage_D.nexus
        File nex = new File(macroDir.toFile(), "MacroLanguage_D.nexus");
        //Create file : MacroLanguage_psi.trees
        File trees = new File(macroDir.toFile(), "MacroLanguage_psi.trees");

        assertTrue(nex.exists(), "SLPhy output file does not exist : " + nex);
        assertTrue(trees.exists(), "SLPhy output file does not exist : " + trees);

        final String expect = "ntax=10";
        try {
            assertTrue(Files.lines(nex.toPath()).anyMatch(l -> l.contains(expect)),
                    nex + " should contain " + expect);
            assertTrue(Files.lines(trees.toPath()).anyMatch(l -> l.contains(expect)),
                    trees + " should contain " + expect);
        } catch (IOException e) {
            fail(e);
        }


    }
}