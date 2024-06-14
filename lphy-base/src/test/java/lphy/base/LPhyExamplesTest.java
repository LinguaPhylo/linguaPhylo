package lphy.base;

import lphy.core.io.UserDir;
import lphy.core.model.Value;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModelUtils;
import lphy.core.simulator.Sampler;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
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
            if (dir.isDirectory() && !"todo".equalsIgnoreCase(dir.getName()))
                testLPhyExamplesInDir(dir);
        }
    }

    public void testLPhyExamplesInDir(File exampleDir) {
        System.out.println("\nTest that Examples parse in " + exampleDir.getAbsolutePath());
        String[] exampleFiles = exampleDir.list((dir1, name) -> name.endsWith(".lphy"));

        List<String> ignoreFiles = Arrays.asList(
                // TODO random fail
                //                "birthDeathOnRootAgeAndTaxa.lphy", "birthDeathRhoSampling.lphy"
                // TODO take too long ?
                //                "covidDPG.lphy"
        );
        //            String fileName = "hcv_coal_classic.lphy";

        List<String> failedByParser = new ArrayList<>();
        List<String> failedBySample = new ArrayList<>();
        for (String fileName : Objects.requireNonNull(exampleFiles)) {
            System.out.println("Processing " + fileName + " in " + exampleDir);
            if (ignoreFiles.contains(fileName)){
                System.out.println("Skip testing " + fileName + " ! ");
                break;
            }

            UserDir.setUserDir(exampleDir.getPath());
            LPhyParserDictionary lPhyMetaParser = new REPL();
            try {
                FileReader lphyFile = new FileReader(exampleDir.getAbsoluteFile() + File.separator + fileName);
                BufferedReader fin = new BufferedReader(lphyFile);
                lPhyMetaParser.source(fin, null);
            } catch (Exception e) {
                failedByParser.add(fileName);
                System.err.println("Example " + fileName + " failed during parsing !!! \n");
                e.printStackTrace();
            }
            // lines of code parsed
            List<String> lines = lPhyMetaParser.getLines();
            assertTrue(lines.size() > 0);

            String cmd = String.join("", lPhyMetaParser.getLines());
            // check lines
            assertTrue(cmd.trim().length() > 3, "Script must contain more than 3 characters : \n" + cmd);

            System.out.println("Successfully parse " + fileName + "\n");

            //*** Test re-sampling ***//
            List<Value> res1 = GraphicalModelUtils.getAllValuesFromSinks(lPhyMetaParser);
            final int nAllVal = res1.size();
            Sampler sampler = new Sampler(lPhyMetaParser);
            for (int i = 0; i < 2; i++) {
                try {
                    List<Value> res = sampler.sample(null); // random seed
                    assertEquals(nAllVal, res.size(), "Resample " + fileName +
                            ", but the returned values ");
                } catch (Exception e) {
                    if (! failedBySample.contains(fileName))
                        failedBySample.add(fileName);
                    System.err.println("Example " + fileName + " failed during re-sampling !!! \n");
                    e.printStackTrace();
                }
            }

            // clean parser dict for reusing it safely
            lPhyMetaParser.clear();
        }

        if (!failedByParser.isEmpty() || !failedBySample.isEmpty()) {
            fail("\nFailed LPhy scripts by parsing : " + failedByParser +
                    "\nFailed LPhy scripts by resampling : " + failedBySample);
        } else {
            System.out.println("SUCCESS!!!");
            System.out.println(exampleFiles.length + " files tested : \n" + Arrays.toString(exampleFiles));
            System.out.println(ignoreFiles.size() + " files ignored : \n" + ignoreFiles);
        }
        //            assertEquals(0, failedFiles.size(), failedFiles.toString());
    } // testLPhyExamplesInDir

}
