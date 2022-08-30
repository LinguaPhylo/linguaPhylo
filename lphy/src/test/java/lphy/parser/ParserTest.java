package lphy.parser;

import lphy.core.LPhyParser;
import lphy.system.UserDir;
import org.junit.jupiter.api.BeforeEach;
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

public class ParserTest {

    LPhyParser lPhyParser;

    @BeforeEach
    public void setUp() throws Exception {
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
	
	private Object parse(String cmd) {
		SimulatorListenerImpl parser = new SimulatorListenerImpl(lPhyParser, LPhyParser.Context.model);
		if (!cmd.endsWith(";")) {
			cmd = cmd + ";";
		}
		Object o = parser.parse(cmd);
		return o;
	}


    @Test
    public void testLPhyExamplesRun() {
        final String wd = System.getProperty("user.dir");
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
        try {
            System.out.println("\nTest that Examples parse in " + exampleDir.getAbsolutePath());
            String[] exampleFiles = exampleDir.list((dir1, name) -> name.endsWith(".lphy"));

            List<String> failedFiles = new ArrayList<String>();
//            String fileName = "hcv_coal_classic.lphy";
            for (String fileName : Objects.requireNonNull(exampleFiles) ) {
                System.out.println("Processing " + fileName);
                UserDir.setUserDir(exampleDir.getPath());
                try {
                    FileReader lphyFile = new FileReader(exampleDir.getAbsoluteFile() + File.separator + fileName);
                    BufferedReader fin = new BufferedReader(lphyFile);
                    lPhyParser = new REPL();
                    lPhyParser.source(fin);
                    lPhyParser.clear();
//	                StringBuffer buf = new StringBuffer();
//	                String str = null;
//	                while (fin.ready()) {
//	                    str = fin.readLine();
//	                    buf.append(str);
//	                    buf.append('\n');
//	                }
//	                fin.close();
//	                parse(buf.toString());
                } catch (Exception e) {
                    System.out.println("ExampleParsing::Failed for " + fileName + ": " + e.getMessage());
                    failedFiles.add(fileName);
                }
                //TODO assert if any Exception
                System.out.println("Done " + fileName + "\n");
            }
            if (failedFiles.size() > 0) {
                System.out.println("\ntestThatExamplesRun::Failed for : " + failedFiles);
            } else {
                System.out.println("SUCCESS!!!");
                System.out.println(exampleFiles.length + " file tested : \n" + Arrays.toString(exampleFiles));
            }
            assertEquals(0, failedFiles.size(), failedFiles.toString());
        } catch (Exception e) {
            System.out.println("exception thrown ");
            System.out.println(e.getMessage());
        }
    } // testLPhyExamplesInDir
    
}
