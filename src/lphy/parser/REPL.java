package lphy.parser;


import java.io.*;
import java.util.*;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Value;

/** A simple Read-Eval-Print-Loop for the graphicalModelSimulator language **/ 
public class REPL implements LPhyParser {
	SortedMap<String, Value<?>> dictionary = new TreeMap<>();

	private List<String> lines = new ArrayList<>();

	public REPL() {
	}

	public void doREPL() {
		while (true) {
			System.out.print(">");
			try {
				String cmd = (new BufferedReader(new InputStreamReader(System.in))).readLine();
				parse(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<String, Value<?>> getDictionary() {
		return dictionary;
	}

	public void parse(String cmd) {
		if (cmd == null) {
			return;
		}
		if (cmd.startsWith("quit") || cmd.startsWith("end")) {
			System.exit(0);
		} else if (cmd.trim().length() == 0) {
			// ignore empty lines
		} else if (!cmd.startsWith("?")) {
			try {
				SimulatorListenerImpl parser = new SimulatorListenerImpl(dictionary);
				if (!cmd.endsWith(";")) {
					cmd = cmd + ";";
				}
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
		lines.add(cmd);
	}

	@Override
	public Map<String, Set<Class<?>>> getGenerativeDistributionClasses() {
		return SimulatorListenerImpl.genDistDictionary;
	}

	@Override
	public List<String> getLines() {
		return lines;
	}

	@Override
	public void clear() {
		dictionary.clear();
		lines.clear();
	}


	public static void main(String[] args) {
		System.out.println("A  simple Read-Eval-Print-Loop for the lphy language ");
		REPL repl = new REPL();
		repl.doREPL();
	}


}
