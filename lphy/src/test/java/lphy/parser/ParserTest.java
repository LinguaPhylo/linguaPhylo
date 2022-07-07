package lphy.parser;

import lphy.core.LPhyParser;
import lphy.system.UserDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void testThatExamplesRun() {
        final String dir = System.getProperty("user.dir") + "/../examples";
        test_ThatXmlExamplesRun(dir);
    }
    
    public void test_ThatXmlExamplesRun(String dir) {
        try {
            System.out.println("Test that Examples parse in " + dir);
            File exampleDir = new File(dir);
            String[] exampleFiles = exampleDir.list(new FilenameFilter() {
                @Override
				public boolean accept(File dir, String name) {
                    return name.endsWith(".lphy");
                }
            });

            List<String> failedFiles = new ArrayList<String>();
//            String fileName = "hcv_coal_classic.lphy";
            for (String fileName : Objects.requireNonNull(exampleFiles) ) {
                System.out.println("Processing " + fileName);
                UserDir.setUserDir(dir);
                try {
                    FileReader lphyFile = new FileReader(dir + "/" + fileName);
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
                    System.out.println("ExampleParsing::Failed for " + fileName
                            + ": " + e.getMessage());
                    failedFiles.add(fileName);
                }
                System.out.println("Done " + fileName + "\n");
            }
            if (failedFiles.size() > 0) {
                System.out.println("\ntestThatExamplesRun::Failed for : " + failedFiles.toString());
            } else {
                System.out.println("SUCCESS!!!");
                System.out.println(exampleFiles.length + " file tested : \n" + Arrays.toString(exampleFiles));
            }
            assertTrue(failedFiles.size() == 0, failedFiles.toString());
        } catch (Exception e) {
            System.out.println("exception thrown ");
            System.out.println(e.getMessage());
        }
    } // test_ThatXmlExamplesRun
    
}
