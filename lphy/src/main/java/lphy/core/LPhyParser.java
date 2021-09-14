package lphy.core;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleValue;
import lphy.graphicalModel.types.IntegerValue;
import lphy.parser.SimulatorParsingException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public interface LPhyParser extends GraphicalModel {

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

    default void parse(String code) {
        parse(code, Context.model);
    }

    void parse(String code, Context context) throws SimulatorParsingException;

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
        parse(builder.toString());
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
        private static Map<String, Value<?>> parseArguments(String argumentString, LPhyParser parser) {

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
            } else if (Command.CommandUtils.isLiteral(valueString)) {
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