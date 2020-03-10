package lphy.core;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleValue;
import lphy.graphicalModel.types.IntegerValue;
import lphy.parser.ExpressionNode;
import lphy.parser.ExpressionNodeWrapper;
import lphy.utils.LoggerUtils;

import java.util.*;

public interface LPhyParser {

    /**
     * @return the dictionary of parsed values with id's, keyed by id
     */
    Map<String, Value<?>> getDictionary();

    void addCommand(Command command);

    Collection<Command> getCommands();

    default Set<Value<?>> getSinks() {
        SortedSet<Value<?>> nonArguments = new TreeSet<>(Comparator.comparing(Value::getId));
        getDictionary().values().forEach((val) -> {
            if (!val.isAnonymous() && val.getOutputs().size() == 0) nonArguments.add(val);
        });
        return nonArguments;
    }

    /**
     * @return a list of keywords including names of distributions, functions, commands, and param names.
     */
    default List<String> getKeywords() {
        List<String> keywords = new ArrayList<>();
        keywords.addAll(getGeneratorClasses().keySet());
        return keywords;
    }

    void parse(String code);

    /**
     * @return the classes of generative distributions recognised by this parser, keyed by their name in lphy
     */
    Map<String, Set<Class<?>>> getGeneratorClasses();

    List<String> getLines();

    /**
     * Clears the dictionary and lines of this parser.
     */
    void clear();

    class Utils {

        public static void parseCommand(Command command, String commandString, Map<String, Value<?>> dictionary) {
            commandString = commandString.trim();
            if (!commandString.startsWith(command.getName())) {
                throw new RuntimeException();
            }

            String remainder = commandString.substring(command.getName().length());
            if (remainder.endsWith(";")) {
                remainder = remainder.substring(0, remainder.length() - 1);
            }
            Map<String, Value<?>> arguments =
                    parseArguments(remainder.substring(1, remainder.length() - 1), dictionary);
            command.execute(arguments);
        }

        private static Map<String, Value<?>> parseArguments(String argumentString, Map<String, Value<?>> dictionary) {

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

                Value val = parseValueExpression(valueString, dictionary);
                arguments.put(key, val);
                argumentCount += 1;
            }
            return arguments;
        }

        /**
         * @param valueString a piece of a string that can represent a value. Could be literal or an id of a value or a function call.
         * @return A Value constructed or produced from this expression
         */
        private static Value parseValueExpression(String valueString, Map<String, Value<?>> dictionary) {

            if (dictionary.keySet().contains(valueString)) {
                return dictionary.get(valueString);
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


        /**
         * Wraps recursively defined functions into a single graphical model node.
         *
         * @param parser
         */
        public static void wrapExpressionNodes(LPhyParser parser) {

            int wrappedExpressionNodeCount = 0;
            boolean found = false;
            do {
                for (Value value : parser.getSinks()) {
                    found = wrapExpressionNodes(value);
                    if (found) wrappedExpressionNodeCount += 1;
                }

            } while (found);
        }

        private static boolean wrapExpressionNodes(Value value) {
            for (GraphicalModelNode node : (List<GraphicalModelNode>) value.getInputs()) {
                if (node instanceof ExpressionNode) {
                    ExpressionNode eNode = (ExpressionNode) node;

                    if (ExpressionNodeWrapper.expressionSubtreeSize(eNode) > 1) {
                        ExpressionNodeWrapper wrapper = new ExpressionNodeWrapper((ExpressionNode) node);
                        value.setFunction(wrapper);
                        return true;
                    }
                }
            }
            for (GraphicalModelNode node : (List<GraphicalModelNode>) value.getInputs()) {
                if (node instanceof Generator) {
                    Generator p = (Generator) node;
                    for (GraphicalModelNode v : (List<GraphicalModelNode>) p.getInputs()) {
                        if (v instanceof Value) {
                            return wrapExpressionNodes((Value) v);
                        }
                    }
                }
            }
            return false;
        }


        public static String getCanonicalScript(LPhyParser parser) {
            Set<Value> visited = new HashSet<>();

            StringBuilder builder = new StringBuilder();
            for (Value value : parser.getSinks()) {

                Value.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                    @Override
                    public void visitValue(Value value) {

                        if (!visited.contains(value)) {

                            if (!value.isAnonymous()) {
                                String str = value.codeString();
                                if (!str.endsWith(";")) str += ";";
                                builder.append(str).append("\n");
                            }
                            visited.add(value);
                        }
                    }

                    public void visitGenerator(Generator generator) {
                    }
                }, true);
            }
            return builder.toString();
        }

        public static List<Value<?>> getAllValuesFromSinks(LPhyParser parser) {
            List<Value<?>> values = new ArrayList<>();
            for (Value<?> v : parser.getSinks()) {
                getAllValues(v, values);
            }
            return values;
        }

        private static void getAllValues(GraphicalModelNode<?> node, List<Value<?>> values) {
            if (node instanceof Value && !values.contains(node)) {
                values.add((Value<?>) node);
            }
            for (GraphicalModelNode<?> childNode : node.getInputs()) {
                getAllValues(childNode, values);
            }
        }
    }
}