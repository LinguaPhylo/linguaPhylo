package parser;

import java.util.*;

import org.junit.Test;

import james.parser.SimulatorListenerImpl;
import junit.framework.TestCase;

public class ParserTest extends TestCase {
	
	@Test
	public void testAssingment() {
		parse("a=3;");
		parse("a=2;b=a;");
		parse("a=3;b=3*a;");	
	}
	
	private Object parse(String cmd) {
		SimulatorListenerImpl parser = new SimulatorListenerImpl(new TreeMap());
		if (!cmd.endsWith(";")) {
			cmd = cmd + ";";
		}
		Object o = parser.parse(cmd);
		return o;
	}


}
