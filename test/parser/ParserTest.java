package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.*;

import lphy.core.LPhyParser;
import lphy.parser.REPL;
import org.junit.Test;

import lphy.parser.SimulatorListenerImpl;
import junit.framework.TestCase;

public class ParserTest extends TestCase {
	
	@Test
	public void testAssingment() {
		parse("a=3;");
		parse("a=2;b=a;");
		parse("a=3.0;b=3.0*a;");	
		parse("a=3;b=3.0*a;");	
		parse("a=3.0;b=3*a;");	
	}
	
	private Object parse(String cmd) {
		SimulatorListenerImpl parser = new SimulatorListenerImpl(new REPL(), LPhyParser.Context.model);
		if (!cmd.endsWith(";")) {
			cmd = cmd + ";";
		}
		Object o = parser.parse(cmd);
		return o;
	}


    @Test
    public void testThatExamplesRun() {
        String dir = System.getProperty("user.dir") + "/examples";
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
            for (String fileName : exampleFiles) {
                System.out.println("Processing " + fileName);
                try {
	                BufferedReader fin = new BufferedReader(new FileReader(dir + "/" + fileName));
	                StringBuffer buf = new StringBuffer();
	                String str = null;
	                while (fin.ready()) {
	                    str = fin.readLine();
	                    buf.append(str);
	                    buf.append('\n');
	                }
	                fin.close();
	                parse(buf.toString());
                } catch (Exception e) {
                    System.out.println("ExampleParsing::Failed for " + fileName
                            + ": " + e.getMessage());
                    failedFiles.add(fileName);
                }
                System.out.println("Done " + fileName);
            }
            if (failedFiles.size() > 0) {
                System.out.println("\ntestThatExamplesRun::Failed for : " + failedFiles.toString());
            } else {
                System.out.println("SUCCESS!!!");
            }
            assertTrue(failedFiles.toString(), failedFiles.size() == 0);
        } catch (Exception e) {
            System.out.println("exception thrown ");
            System.out.println(e.getMessage());
            ;
        }
    } // test_ThatXmlExamplesRun
    
}
