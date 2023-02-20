package lphy.parser;

import lphy.core.LPhyParser;
import lphy.system.UserDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    LPhyParser lPhyParser;

    @BeforeEach
    public void setUp() {
        lPhyParser = new REPL();
    }

    @Test
    public void testAssingment() {
        parse("a=3;");
        parse("a=2;b=a;");
        parse("a=3.0;b=3.0*a;");
        parse("a=3;b=3.0*a;");
        parse("a=3.0;b=3*a;");
    }

    @Test
    public void testSemicolon() {
        parse("a=3");
        parse("a=2;b=a");
    }

    private void parse(String cmd) {
        Object o = null;
        try {
            SimulatorListenerImpl parser = new SimulatorListenerImpl(lPhyParser, LPhyParser.Context.model);
            o = parser.parse(cmd);
        } catch (Exception e) {
            fail("CMD " + cmd + " failed to parse, Exception :\n" + e.getMessage());
        }
        assertNotNull(o);
    }


    @Test
    public void testLPhyExamplesRun() {
        final String wd = System.getProperty("user.dir");
        // github workflow run tests in $project_root/lphy, so "**/lphy/../examples" is required
        final File exampleDir = Paths.get(wd, "..", "examples").toFile();
        assertTrue(exampleDir.exists(), "Cannot find examples folder : " + exampleDir);

        testLPhyExamplesInDir(exampleDir);

        File[] dirs = exampleDir.listFiles();
        assertNotNull(dirs);
        for (final File dir : dirs) {
            if (dir.isDirectory())
                testLPhyExamplesInDir(dir);
        }
    }

    public void testLPhyExamplesInDir(File exampleDir) {
        System.out.println("\nTest that Examples parse in " + exampleDir.getAbsolutePath());
        String[] exampleFiles = exampleDir.list((dir1, name) -> name.endsWith(".lphy"));

//            List<String> failedFiles = new ArrayList<String>();
//            String fileName = "hcv_coal_classic.lphy";
        for (String fileName : Objects.requireNonNull(exampleFiles)) {
            System.out.println("Processing " + fileName + " in " + exampleDir);
            if (fileName.equals("jcSimData2.lphy"))
                break;

            UserDir.setUserDir(exampleDir.getPath());
            lPhyParser = new REPL();
            try {
                FileReader lphyFile = new FileReader(exampleDir.getAbsoluteFile() + File.separator + fileName);
                BufferedReader fin = new BufferedReader(lphyFile);
                lPhyParser.source(fin);
            } catch (Exception e) {
//                    failedFiles.add(fileName);
                System.err.println("Example " + fileName + " failed\n");
                fail("Example " + fileName + " failed at Exception :\n" + e.getMessage());
            }
            // lines of code parsed
            List<String> lines = lPhyParser.getLines();
            assertTrue(lines.size() > 0);

            String cmd = String.join("", lPhyParser.getLines());
            // check lines
            assertTrue(cmd.trim().length() > 3, "Script must contain more than 3 characters : \n" + cmd);

            lPhyParser.clear();
            System.out.println("Done " + fileName + "\n");
        }
//            if (failedFiles.size() > 0) {
//                System.out.println("\ntestThatExamplesRun::Failed for : " + failedFiles);
//            } else {
        System.out.println("SUCCESS!!!");
        System.out.println(exampleFiles.length + " file tested : \n" + Arrays.toString(exampleFiles));
//            }
//            assertEquals(0, failedFiles.size(), failedFiles.toString());
    } // testLPhyExamplesInDir

}
