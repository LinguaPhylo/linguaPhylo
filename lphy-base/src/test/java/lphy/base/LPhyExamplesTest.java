package lphy.base;

import lphy.core.codebuilder.CanonicalCodeBuilder;
import lphy.core.io.UserDir;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModelUtils;
import lphy.core.simulator.RandomUtils;
import lphy.core.simulator.Sampler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LPhyExamplesTest {

    File exampleDir;
    File tutorialDir;

    @BeforeEach
    void setUp() {
        final String wd = System.getProperty("user.dir");
        // github workflow run tests in $project_root/lphy, so "**/lphy/../examples" is required
        exampleDir = Paths.get(wd, "..", "examples").toFile();
        assertTrue(exampleDir.exists(), "Cannot find examples folder : " + exampleDir);

        tutorialDir = Paths.get(wd, "..", "tutorials").toFile();
        assertTrue(tutorialDir.exists(), "Cannot find tutorial folder : " + tutorialDir);

        RandomUtils.setSeed(666L);
    }

    @Test
    public void testLPhyExamplesRun() {

        // lphy scripts under this dir
        testLPhyExamplesInDir(exampleDir);

        // scripts under the sub-folders
        File[] dirs = exampleDir.listFiles();
        assertNotNull(dirs);
        for (final File dir : dirs) {
            if (dir.isDirectory() && !"todo".equalsIgnoreCase(dir.getName()))
                testLPhyExamplesInDir(dir);
        }

        testLPhyExamplesInDir(tutorialDir);
    }

    /**
     * Check the values are sampled correctly from both parser and {@link Sampler#sample(Long)}.
     * @param exampleDir   folder to contain lphy script, no recursive.
     */
    protected void testLPhyExamplesInDir(File exampleDir) {
        System.out.println("\nTest that Examples parse in " + exampleDir.getAbsolutePath());
        String[] exampleFiles = exampleDir.list((dir1, name) -> name.endsWith(".lphy"));

        List<String> ignoreFiles = Arrays.asList(
                // give the lphy file name here, if it is not tested
        );

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
                // Display in stdout
                System.out.println("Err: example " + fileName + " failed during parsing !!! \n");
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
            final long[] seeds = new long[]{666L, 777L};

            List<Value> res1 = GraphicalModelUtils.getAllValuesFromSinks(lPhyMetaParser);
            final int nAllVal = res1.size();
            Sampler sampler = new Sampler(lPhyMetaParser);
            for (long seed : seeds) {
                try {
                    List<Value> res;
                    // to avoid memory issue of random sampling BD trees
                    if (exampleDir.getPath().endsWith("birth-death"))
                        res = sampler.sample(seed); // only for birth death
                    else
                        res = sampler.sample(null); // random seed

                    assertEquals(nAllVal, res.size(), "Resample " + fileName +
                            ", but the returned values ");
                } catch (Exception e) {
                    if (!failedBySample.contains(fileName)) failedBySample.add(fileName);
                    // Display in stdout
                    System.out.println("Err: example " + fileName + " failed during re-sampling !!! \n");
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

    @Test
    public void testCodeBuilder() {

        // lphy scripts under this dir
        testCodeBuilder(exampleDir);

        // scripts under the sub-folders
        File[] dirs = exampleDir.listFiles();
        assertNotNull(dirs);
        for (final File dir : dirs) {
            if (dir.isDirectory() && !"todo".equalsIgnoreCase(dir.getName()) )
                testCodeBuilder(dir);
        }

        testCodeBuilder(tutorialDir);
    }

    // test if the lphy script can be reversible by CanonicalCodeBuilder to all example scripts.
    protected void testCodeBuilder(File exampleDir) {
        System.out.println("\nTest that examples are revisable using CodeBuilder in " + exampleDir.getAbsolutePath());
        String[] exampleFiles = exampleDir.list((dir1, name) -> name.endsWith(".lphy"));
        List<String> ignoreFiles = Arrays.asList(
                "cpacific.lphy", // tutorials/cpacific.lphy ==> array lengths differ,
                "vars-in-array.lphy", //TODO
                "jcc2Fasta.lphy" //TODO
        );

        CanonicalCodeBuilder codeBuilder = new CanonicalCodeBuilder();
        List<String> failed = new ArrayList<>();
        for (String fileName : Objects.requireNonNull(exampleFiles)) {
            System.out.println("Processing " + fileName + " in " + exampleDir);
            if (ignoreFiles.contains(fileName)){
                System.out.println("Skip testing " + fileName + " ! ");
                break;
            }

            UserDir.setUserDir(exampleDir.getPath());
            LPhyParserDictionary originalDict = new REPL();
            Path lphyPath = Paths.get(exampleDir.getAbsolutePath(), fileName);
            try {
                FileReader lphyFile = new FileReader(lphyPath.toString());
                BufferedReader fin = new BufferedReader(lphyFile);
                originalDict.source(fin, null);

            } catch (Exception e) {
                failed.add(fileName);
                // Display in stdout
                System.out.println("Err: example " + fileName + " failed during parsing !!! \n");
                e.printStackTrace();
            }

            String lphyCode = codeBuilder.getCode(originalDict);

            LPhyParserDictionary newDict = new REPL();
            newDict.parse(lphyCode);

            // *** compare keys
            Map<String, Value<?>> treeMap1 = new TreeMap<>(originalDict.getDataDictionary());
            Map<String, Value<?>> treeMap2 = new TreeMap<>(newDict.getDataDictionary());

            assertArrayEquals(treeMap1.keySet().toArray(new String[0]), treeMap2.keySet().toArray(new String[0]),
                    "Original data dict : " + treeMap1 + "\nBut data dict after code builder : " +
                            treeMap2 + "\nFile = " + lphyPath);

            Map<String, Value<?>> treeMap3 = new TreeMap<>(originalDict.getModelDictionary());
            Map<String, Value<?>> treeMap4 = new TreeMap<>(newDict.getModelDictionary());

            assertArrayEquals(treeMap3.keySet().toArray(new String[0]), treeMap4.keySet().toArray(new String[0]),
                    "Original model dict : " + treeMap3 + "\nBut model dict after code builder : " +
                            treeMap4 + "\nFile = " + lphyPath);

            // *** compare values, but not RandomVariable
            String[] vals1 = GraphicalModelUtils.getAllValuesFromSinks(originalDict).stream().
                    filter(v -> !(v instanceof RandomVariable)).map(Value::codeString).toList().toArray(new String[0]);
            String[] vals2 = GraphicalModelUtils.getAllValuesFromSinks(newDict).stream().
                    filter(v -> ! (v instanceof RandomVariable)).map(Value::codeString).toList().toArray(new String[0]);

            assertArrayEquals(vals1, vals2, "Original data value set : " + Arrays.toString(vals1) +
                    "\nBut data value set after code builder : " + Arrays.toString(vals2) + "\nFile = " + lphyPath);
        }
    }// testCodeBuilder

}
