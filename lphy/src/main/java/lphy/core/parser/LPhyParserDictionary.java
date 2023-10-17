package lphy.core.parser;

import lphy.core.exception.SimulatorParsingException;
import lphy.core.model.ExpressionUtils;
import lphy.core.model.Value;
import lphy.core.model.datatype.DoubleValue;
import lphy.core.model.datatype.IntegerValue;
import lphy.core.parser.graphicalmodel.GraphicalModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The interface to guide the classes storing the LPhy objects
 * parsed by ANTRL parser, or lPhy code lines.
 * There are two 'parse' methods:
 * the 1st is used for scripts loaded from files,
 * which uses data/model keywords to define the block.
 * The 2nd method is used for studio console,
 * where the data/model block is determined by buttons in GUI.
 * @see REPL
 */
public interface LPhyParserDictionary extends GraphicalModel {

    // the name of the code being parsed.
    String getName();

    /**
     * @param type the type of value
     * @return a list of all the declared values assignable to the given type.
     */
    default List<Value<?>> getNamedValuesByType(Class<?> type) {
        List<Value<?>> valuesByType = getDataDictionary().values().stream().filter(v -> (type.isAssignableFrom(v.value().getClass()) && !v.isAnonymous())).collect(Collectors.toList());

        valuesByType.addAll(getModelDictionary().values().stream().filter(v -> (type.isAssignableFrom(v.value().getClass()) && !v.isAnonymous())).collect(Collectors.toList()));

        return valuesByType;
    }

    /**
     * @return a list of keywords including names of distributions, functions, commands, and param names.
     */
    default List<String> getKeywords() {
        List<String> keywords = new ArrayList<>();
        keywords.addAll(getGeneratorClasses().keySet());
        return keywords;
    }

    /**
     * This is only used to parse the lphy code loaded from a file,
     * the studio console cmd is parsed by a different method,
     * because its cmd does not keep the data and model keywords.
     * @param lphyCode
     * @throws SimulatorParsingException
     * @throws IllegalArgumentException
     */
    void parseScript(String lphyCode) throws SimulatorParsingException,IllegalArgumentException;

    default Object parseConsoleCMD(String lphyCode, LPhyParserDictionary.Context context) {
        if (lphyCode == null || lphyCode.trim().isEmpty()) {
            // ignore empty lines
            return null;
        } else if (!lphyCode.startsWith("?")) {
            // either 1 lphyCode each line, or all cmds in 1 line
            LPhyListenerImpl parser = new LPhyListenerImpl(this);
            // set context
            return parser.parse(lphyCode, context);
        } else throw new RuntimeException();
    }

    /**
     * @return the classes of generative distributions recognised by this parser, keyed by their name in lphy
     */
    Map<String, Set<Class<?>>> getGeneratorClasses();

    List<String> getLines();

    /**
     * Clears the dictionary and lines of this parser.
     */
    void clear();

    /**
     * @param sourceFile the lphy source file
     */
    default void source(File sourceFile) throws IOException {
        FileReader reader = new FileReader(sourceFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        source(bufferedReader);
        reader.close();
    }

    /**
     * @param bufferedReader the lphy source
     */
    default void source(BufferedReader bufferedReader) throws IOException {
        StringBuilder builder = new StringBuilder();

        String line = bufferedReader.readLine();
        while (line != null) {
            builder.append(line);
            builder.append("\n");
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        parseScript(builder.toString());
    }

    /**
     *  Start with canonical id, if that is null call value.getUniqueId(), and wrap in single quotes if this is a data value that is clamped in the model.
     * @param value the value
     * @return a unique id string for this value.
     */
    default String getUniqueId(Value value) {
        String uniqueId = value.getCanonicalId();
        if (uniqueId == null) uniqueId = value.getUniqueId();
        if (isClamped(value.getId()) && isNamedDataValue(value)) {
            uniqueId = "'" + uniqueId + "'";
        }
        return uniqueId;
    }

    class Utils {
        /**
         * Parses the arguments of a command. A command can't be in the data context.
         *
         * @param argumentString
         * @param parser
         * @return
         */
        private static Map<String, Value<?>> parseArguments(String argumentString, LPhyParserDictionary parser) {

            String[] argumentStrings = splitArgumentString(argumentString);

            TreeMap<String, Value<?>> arguments = new TreeMap<>();
            int argumentCount = 0;
            for (int i = 0; i < argumentStrings.length; i++) {

                String argumentPair = argumentStrings[i];
                System.out.println("arg" + i + ": " + argumentPair);

                if (argumentPair.indexOf('=') < 0) {
                    argumentPair = argumentCount + "=" + argumentPair;
                }
                int pos = argumentPair.indexOf('=');

                String key = argumentPair.substring(0, pos).trim();
                String valueString = argumentPair.substring(pos + 1).trim();

                Value val = parseValueExpression(valueString, parser, Context.model);
                arguments.put(key, val);
                argumentCount += 1;
            }
            return arguments;
        }

        /**
         * @param valueString a piece of a string that can represent a value. Could be literal or an id of a value or a function call.
         * @return A Value constructed or produced from this expression
         */
        private static Value parseValueExpression(String valueString, GraphicalModel model, Context context) {

            Value<?> val = model.getValue(valueString, context);

            if (val != null) {
                return val;
            } else if (ExpressionUtils.isLiteral(valueString)) {
                return parseLiteralValue(null, valueString);
            }
//            else if (isFunction(valueString)) {
//                return parseDeterministicFunction(null, valueString, lineNumber);
//            }
            else throw new RuntimeException("Failed to parse value expression " + valueString);
        }

        private static Value parseLiteralValue(String id, String valueString) {

            if (valueString.startsWith("\"") && valueString.endsWith("\"")) {
                // parse string
                return new Value<>(id, valueString.substring(1, valueString.length() - 1));
            }

//            if (valueString.startsWith("[") && valueString.endsWith("]")) {
//                return parseList(id, valueString);
//            }

            try {
                Integer intVal = Integer.parseInt(valueString);
                return new IntegerValue(id, intVal);
            } catch (NumberFormatException ignored) {
            }

            try {
                Double val = Double.parseDouble(valueString);
                return new DoubleValue(id, val);
            } catch (NumberFormatException ignored) {
            }

            try {
                Boolean booleanValue = Boolean.parseBoolean(valueString);
                return new Value<>(id, booleanValue);
            } catch (NumberFormatException ignored) {
            }

            throw new RuntimeException("Parsing fixed parameter " + id + " with value " + valueString);
        }

        private static String[] splitArgumentString(String argumentString) {

            if (argumentString.length() == 0) return new String[]{};

            String argumentSplitterPattern =
                    ",(?=(([^']*'){2})*[^']*$)(?=(([^\"]*\"){2})*[^\"]*$)(?![^()]*\\))(?![^\\[]*\\])";

            return argumentString.split(argumentSplitterPattern);
        }
    }
}