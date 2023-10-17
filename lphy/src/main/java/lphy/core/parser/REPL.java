package lphy.core.parser;


import lphy.core.exception.SimulatorParsingException;
import lphy.core.model.Value;
import lphy.core.parser.graphicalmodel.GraphicalModelUtils;
import lphy.core.spi.LoaderManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * A simple Read-Eval-Print-Loop for the LPhy language
 **/
public class REPL implements LPhyParserDictionary {

    SortedMap<String, Value<?>> modelDictionary = new TreeMap<>();
    SortedMap<String, Value<?>> dataDictionary = new TreeMap<>();

    Set<Value> modelValues = new HashSet<>();
    Set<Value> dataValues = new HashSet<>();

    String name = null;
//TODO merge commands and lines, and may have data lines + model lines
    SortedMap<String, Command> commands = new TreeMap<>();

    private List<String> lines = new ArrayList<>();

    public REPL() {
    }

    public void doREPL() {
        while (true) {
            System.out.print(">");
            try {
                String cmd = (new BufferedReader(new InputStreamReader(System.in))).readLine();
                parseScript(cmd);
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

    public Set<Value> getDataValues() {
        return dataValues;
    }

    public void parseScript(String lphyCode) throws SimulatorParsingException,IllegalArgumentException {
        if (lphyCode == null) {
            return;
        }

        //TODO: review the code about commands, SortedMap is always empty
        final String commandString = lphyCode;
        final boolean[] found = new boolean[1];
        commands.forEach((key, command) -> {
            if (commandString.startsWith(key)) {
                //TODO why throw UnsupportedOperationException ?
                command.execute(commandString, this);
                found[0] = true;
            }
        });

        if (!found[0]) {
            if (lphyCode.trim().length() == 0) {
                // ignore empty lines
                return;
            } else if (!lphyCode.startsWith("?")) {
                // either 1 lphyCode each line, or all cmds in 1 line
                LPhyListenerImpl parser = new LPhyListenerImpl(this);
                Object o = parser.parse(lphyCode);
                //parser.parse(lphyCode);
                lines.add(lphyCode);

                // wrap the ExpressionNodes before returning from parse
                GraphicalModelUtils.wrapExpressionNodes(this);
            } else throw new RuntimeException();
        }
    }



    @Override
    public Map<String, Set<Class<?>>> getGeneratorClasses() {
        SortedMap<String, Set<Class<?>>> generatorClasses = new TreeMap<>();

        generatorClasses.putAll(LoaderManager.getGenDistDictionary());
        generatorClasses.putAll(LoaderManager.getFunctionDictionary());

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

    public static void main(String[] args) {
        System.out.println("A  simple Read-Eval-Print-Loop for the lphy language ");
        REPL repl = new REPL();
        repl.doREPL();
    }


}
