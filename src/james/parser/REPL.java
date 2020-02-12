package james.parser;


import java.io.*;
import java.util.List;

/** A simple Read-Eval-Print-Loop for the graphicalModelSimulator language **/ 
public class REPL {

	public REPL() {
	}
	

	public void doREPL() {
		while (true) {
			System.out.print(">");
			try {
				String cmd = (new BufferedReader(new InputStreamReader(System.in))).readLine();
				processCmd(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void processCmd(String cmd) {
		if (cmd == null) {
			return;
		}
		if (cmd.startsWith("quit") || cmd.startsWith("end") || cmd == null) {
			System.exit(0);
		} else if (cmd.startsWith("?")) {
			try {
				SimulatorListenerImpl parser = new SimulatorListenerImpl();
				Object o = parser.parse(cmd);
				//parser.parse(cmd);
			} catch (SimulatorParsingException e) {
				System.out.println(cmd);
				System.out.println(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace(System.err);
				System.err.println("Error: " + e.getMessage());
			}
		}
	}


	public static void main(String[] args) {
		System.out.println("A  simple Read-Eval-Print-Loop for the graphicalModelSimulator language ");
		REPL repl = new REPL();
		repl.doREPL();
	}


}
