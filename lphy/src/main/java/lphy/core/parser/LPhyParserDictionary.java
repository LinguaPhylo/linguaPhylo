package lphy.core.parser;

import lphy.core.exception.SimulatorParsingException;
import lphy.core.io.MacroProcessor;
import lphy.core.model.Value;
import lphy.core.parser.graphicalmodel.GraphicalModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * The interactive parser interface.
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
     * interactive parsing method, which allows to add the value into data/model dictionary on real-time.
     * @param lphyCode
     * @throws SimulatorParsingException
     * @throws IllegalArgumentException
     */
    void parse(String lphyCode) throws SimulatorParsingException,IllegalArgumentException;

//    default Object parse(String lphyCode) {
//        if (lphyCode == null || lphyCode.trim().isEmpty()) {
//            // ignore empty lines
//            return null;
//        } else if (!lphyCode.startsWith("?")) {
//            // either 1 lphyCode each line, or all cmds in 1 line
//            LPhyListenerImpl parser = new LPhyListenerImpl(this);
//            // set context
//            return parser.parse(lphyCode);
//        } else throw new RuntimeException();
//    }

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
     * @param constants      constants inputted by user using macro
     */
    default void source(File sourceFile, String[] constants) throws IOException {
        FileReader reader = new FileReader(sourceFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        source(bufferedReader, constants);
        reader.close();
    }

    /**
     * This should be the method used by any application to parse lphy.
     * Before {@link #parse(String)}, the line will be preprocessed by
     * {@link MacroProcessor}, so that the marco templating language
     * will be replaced by the actual values.
     *
     * @param bufferedReader the lphy source
     * @param constants      constants inputted by user (e.g. command line arguments),
     *                       where each string element looks like n=10.
     *                       Null, if no user input.
     */
    default void source(BufferedReader bufferedReader, String[] constants) throws IOException {
        if (bufferedReader == null)
            throw new IOException("BufferedReader is null !");

        // init processor by use input if there is any
        MacroProcessor macroProcessor = new MacroProcessor(constants);

        StringBuilder builder = new StringBuilder();
        String lineProcessed;
        String line = bufferedReader.readLine();
        while (line != null) {
            // process macro here
            lineProcessed = macroProcessor.process(line);
            builder.append(lineProcessed);
            builder.append("\n");

            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        // after macro processed
        // throws SimulatorParsingException,IllegalArgumentException
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
        if (isObserved(value.getId()) && isNamedDataValue(value)) {
            uniqueId = "'" + uniqueId + "'";
        }
        return uniqueId;
    }

    class Utils {

        /**
         * Preferences to choose either "Sample values from parser",
         * or resample values from Dictionary.
         */

        static Preferences preferences = Preferences.userNodeForPackage(REPL.class);
        public static final String SAMPLE_FROM_PARSER = "Sample values using parser";
        private static boolean sampleValuesUsingParser = preferences.getBoolean(SAMPLE_FROM_PARSER, true);

        public static boolean isSampleValuesUsingParser() {
            return sampleValuesUsingParser;
        }

        public static void setSampleValuesUsingParser(boolean sampleValuesUsingParser) {
            sampleValuesUsingParser = sampleValuesUsingParser;
        }


        /**
         * Everything below is in the parser generated by ANTRL grammar {@link LPhyListenerImpl.LPhyASTVisitor}.
         */

        /**
         * Parses the arguments of a command. A command can't be in the data context.
         *
         * @param argumentString
         * @param parser
         * @return

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
            } catch (NumberFormatException ignored) { // Bug: parseBoolean does not throw NumberFormatException
            }
            // this is not reached, parseBoolean == return "true".equalsIgnoreCase(s);
            throw new RuntimeException("Parsing fixed parameter " + id + " with value " + valueString);
        }

        private static String[] splitArgumentString(String argumentString) {

            if (argumentString.length() == 0) return new String[]{};

            String argumentSplitterPattern =
                    ",(?=(([^']*'){2})*[^']*$)(?=(([^\"]*\"){2})*[^\"]*$)(?![^()]*\\))(?![^\\[]*\\])";

            return argumentString.split(argumentSplitterPattern);
        }*/
    }
}