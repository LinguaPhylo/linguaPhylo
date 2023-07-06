package lphy.base;

import lphy.core.io.UserDir;
import lphy.core.parser.LPhyMetaParser;
import lphy.core.parser.REPL;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class LPhyExamplesTest {

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

        List<String> ignoreFiles = Arrays.asList("jcSimData2.lphy",
                // simulate is in lphy-io
                "jcCoal.lphy",
                // TODO gradle test bug : SPI of SequenceType cannot be loaded
                "covidDPG.lphy", "simpleSerialCoalescentNex.lphy", "twoPartitionCoalescentNex.lphy");
//            String fileName = "hcv_coal_classic.lphy";
        for (String fileName : Objects.requireNonNull(exampleFiles)) {
            System.out.println("Processing " + fileName + " in " + exampleDir);
            if (ignoreFiles.contains(fileName)){
                System.out.println("Skip testing " + fileName + " ! ");
                break;
            }

            UserDir.setUserDir(exampleDir.getPath());
            LPhyMetaParser lPhyMetaParser = new REPL();
            try {
                FileReader lphyFile = new FileReader(exampleDir.getAbsoluteFile() + File.separator + fileName);
                BufferedReader fin = new BufferedReader(lphyFile);
                lPhyMetaParser.source(fin);
            } catch (Exception e) {
//                    failedFiles.add(fileName);
                System.err.println("Example " + fileName + " failed\n");
                fail("Example " + fileName + " failed at Exception :\n" + e.getMessage());
            }
            // lines of code parsed
            List<String> lines = lPhyMetaParser.getLines();
            assertTrue(lines.size() > 0);

            String cmd = String.join("", lPhyMetaParser.getLines());
            // check lines
            assertTrue(cmd.trim().length() > 3, "Script must contain more than 3 characters : \n" + cmd);

            lPhyMetaParser.clear();
            System.out.println("Done " + fileName + "\n");
        }
//            if (failedFiles.size() > 0) {
//                System.out.println("\ntestThatExamplesRun::Failed for : " + failedFiles);
//            } else {
        System.out.println("SUCCESS!!!");
        System.out.println(exampleFiles.length + " files tested : \n" + Arrays.toString(exampleFiles));
        System.out.println(ignoreFiles.size() + " files ignored : \n" + ignoreFiles);
//            }
//            assertEquals(0, failedFiles.size(), failedFiles.toString());
    } // testLPhyExamplesInDir

}
