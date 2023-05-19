package lphy.parser;


import lphy.core.LPhyParser;
import lphy.core.Script;
import lphy.graphicalModel.Command;
import lphy.graphicalModel.GraphicalModel;
import lphy.graphicalModel.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * A simple Read-Eval-Print-Loop for the graphicalModelSimulator language
 **/
public class REPL implements LPhyParser {

    SortedMap<String, Value<?>> modelDictionary = new TreeMap<>();
    SortedMap<String, Value<?>> dataDictionary = new TreeMap<>();

    Set<Value> modelValues = new HashSet<>();
    Set<Value> dataValues = new HashSet<>();

    String name = null;

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
            } catch (IOException | SimulatorParsingException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Map<String, Value<?>> getDataDictionary() {
        return dataDictionary;
    }

    @Override
    public Map<String, Value<?>> getModelDictionary() {
        return modelDictionary;
    }

    public Set<Value> getModelValues() {
        return modelValues;
    }

    ;

    public Set<Value> getDataValues() {
        return dataValues;
    }

    ;

    public void parse(String cmd, Context context) throws SimulatorParsingException,IllegalArgumentException {
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
                // either 1 cmd each line, or all cmds in 1 line
                LPhyListenerImpl parser = new LPhyListenerImpl(this, context);
                Object o = parser.parse(cmd);
                //parser.parse(cmd);
                lines.add(cmd);

                // wrap the ExpressionNodes before returning from parse
                GraphicalModel.Utils.wrapExpressionNodes(this);
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

    /**
     * TODO multiple CMDs in 1 line
     * parse string line by line.
     *
     * @param reader assume 1 cmd each line
     * @throws IOException
     */
    @Override
    public void source(BufferedReader reader) throws IOException {

        Script script = Script.loadLPhyScript(reader);
        parse(script.dataLines, LPhyParser.Context.data);
        parse(script.modelLines, LPhyParser.Context.model);
        reader.close();
    }

    public static void main(String[] args) {
        System.out.println("A  simple Read-Eval-Print-Loop for the lphy language ");
        REPL repl = new REPL();
        repl.doREPL();
    }


}
