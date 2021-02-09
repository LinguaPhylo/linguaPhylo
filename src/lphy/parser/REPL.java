package lphy.parser;


import lphy.core.LPhyParser;
import lphy.graphicalModel.Command;
import lphy.graphicalModel.Value;

import java.io.*;
import java.util.*;

/** A simple Read-Eval-Print-Loop for the graphicalModelSimulator language **/ 
public class REPL implements LPhyParser {

	SortedMap<String, Value<?>> modelDictionary = new TreeMap<>();
	SortedMap<String, Value<?>> dataDictionary = new TreeMap<>();

	Set<Value> modelValues = new HashSet<>();
	Set<Value> dataValues = new HashSet<>();

	SortedMap<String, Command> commands = new TreeMap<>();

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

	@Override
	public Map<String, Value<?>> getDataDictionary() {
		return dataDictionary;
	}

	@Override
	public Map<String, Value<?>> getModelDictionary() {
		return modelDictionary;
	}

	public Set<Value> getModelValues() { return modelValues; };

	public Set<Value> getDataValues() { return dataValues; };

	@Override
	public void addCommand(Command command) {
		commands.put(command.getName(), command);
	}

	@Override
	public Collection<Command> getCommands() {
		return commands.values();
	}

	public void parse(String cmd, Context context) {
		if (cmd == null) {
			return;
		}

		final String commandString = cmd;

		final boolean[] found = new boolean[1];
		commands.forEach((key, command) -> {
			if (commandString.startsWith(key)) {
				command.execute(commandString, this);
				found[0] = true;
			}
		});

		if (!found[0]) {
			if (cmd.trim().length() == 0) {
				// ignore empty lines
				return;
			} else if (!cmd.startsWith("?")) {
				try {
					// either 1 cmd each line, or all cmds in 1 line
					SimulatorListenerImpl parser = new SimulatorListenerImpl(this, context);
					if (!cmd.endsWith(";")) {
						cmd = cmd + ";";
					}
					Object o = parser.parse(cmd);
					//parser.parse(cmd);
				} catch (SimulatorParsingException e) {
					System.out.println(cmd);
					System.out.println(e.getMessage());
				} catch (Exception e) {
					System.err.println("Error parsing " + cmd);
					e.printStackTrace(System.err);
				}
				lines.add(cmd);

				// wrap the ExpressionNodes before returning from parse
				LPhyParser.Utils.wrapExpressionNodes(this);
			} else throw new RuntimeException();
		}
	}

	@Override
	public Map<String, Set<Class<?>>> getGeneratorClasses() {
		SortedMap<String, Set<Class<?>>> generatorClasses = new TreeMap<>();

		generatorClasses.putAll(ParserUtils.genDistDictionary);
		generatorClasses.putAll(ParserUtils.functionDictionary);

		return generatorClasses;
	}

	@Override
	public List<String> getLines() {
		return lines;
	}

	@Override
	public void clear() {
		dataDictionary.clear();
		modelDictionary.clear();
		lines.clear();
		dataValues.clear();
		modelValues.clear();
	}

	//*** to identify data{} and model{} ***//

	/** TODO multiple CMDs in 1 line
	 * parse string line by line.
	 * @param reader   assume 1 cmd each line
	 * @see lphy.app.GraphicalModelPanel#source(BufferedReader)
	 * @throws IOException
	 */
	@Override
	public void source(BufferedReader reader) throws IOException {
		String line = reader.readLine();
		LPhyParser.Context context = LPhyParser.Context.model;
		while (line != null) {
			if (line.trim().startsWith("data")) {
				context = LPhyParser.Context.data;
			} else if (line.trim().startsWith("model")) {
				context = LPhyParser.Context.model;
			} else if (line.trim().startsWith("}")) {
				// do nothing as this line is just closing a data or model block.
			} else {
				switch (context) {
					case data:
						parse(line, LPhyParser.Context.data);
						break;
					case model:
						parse(line, LPhyParser.Context.model);
						break;
				}
			}
			line = reader.readLine();
		}
		reader.close();
	}

	public static void main(String[] args) {
		System.out.println("A  simple Read-Eval-Print-Loop for the lphy language ");
		REPL repl = new REPL();
		repl.doREPL();
	}


}
