package lphy.core;

import lphy.app.GraphicalLPhyParser;
import lphy.app.Symbols;
import lphy.core.narrative.Narrative;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleValue;
import lphy.graphicalModel.types.IntegerValue;
import lphy.parser.functions.ExpressionNode;
import lphy.parser.functions.ExpressionNodeWrapper;
import lphy.utils.LoggerUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public interface LPhyParser {

    enum Context {
        data,
        model
    }

    // the name of the code being parsed.
    String getName();

    /**
     * @return the data dictionary of parsed values with id's, keyed by id
     */
    Map<String, Value<?>> getDataDictionary();

    /**
     * @return the model dictionary of parsed values with id's, keyed by id
     */
    Map<String, Value<?>> getModelDictionary();

    Set<Value> getDataValues();

    Set<Value> getModelValues();

    /**
     * @return the value with the given id in the given context, or null if the value id doesn't exist in given context.
     */
    default Value getValue(String id, Context context) {
        switch (context) {
            case data:
                return getDataDictionary().get(id);
            case model:
            default:
                Map<String, Value<?>> data = getDataDictionary();
                Map<String, Value<?>> model = getModelDictionary();
                if (model.containsKey(id)) return model.get(id);
                return data.get(id);
        }
    }

    /**
     * @param type the type of value
     * @return a list of all the declared values assignable to the given type.
     */
    default List<Value<?>> getNamedValuesByType(Class<?> type) {
        List<Value<?>> valuesByType = getDataDictionary().values().stream().filter(v -> (type.isAssignableFrom(v.value().getClass()) && !v.isAnonymous())).collect(Collectors.toList());

        valuesByType.addAll(getModelDictionary().values().stream().filter(v -> (type.isAssignableFrom(v.value().getClass()) && !v.isAnonymous())).collect(Collectors.toList()));

        return valuesByType;
    }

    default boolean hasValue(String id, Context context) {
        return getValue(id, context) != null;
    }

    /**
     * @param id a value id
     * @return true if this id is contained in both the data block and the model block and the model id is a random variable.
     */
    default boolean isClamped(String id) {
        return (id != null && getDataDictionary().containsKey(id) && getModelDictionary().containsKey(id) && getModelDictionary().get(id) instanceof RandomVariable);
    }

    default boolean isClampedVariable(Value value) {
        return value instanceof RandomVariable && isClamped(value.getId());
    }

    void addCommand(Command command);

    Collection<Command> getCommands();

    /**
     * @return all sinks of the graphical model, including in the data block.
     */
    default List<Value<?>> getModelSinks() {
        List<Value<?>> nonArguments = new ArrayList<>();
        getDataDictionary().values().forEach((val) -> {
            if (!val.isAnonymous() && val.getOutputs().size() == 0) nonArguments.add(val);
        });
        getModelDictionary().values().forEach((val) -> {
            if (!val.isAnonymous() && val.getOutputs().size() == 0) nonArguments.add(val);
        });

        nonArguments.sort(Comparator.comparing(Value::getId));

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

    default void parse(String code) {
        parse(code, Context.model);
    }

    void parse(String code, Context context);

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


    class Utils {

        /**
         * @param line a line of LPhy code
         * @return true if the line of code is declaring a random variable.
         */
        public static boolean isRandomVariableLine(String line) {
            return (line.indexOf('~') > 0);
        }

        /**
         * @return a list of all random variables reachable (i.e. that are depended on by) the sinks.
         */
        public static List<RandomVariable<?>> getAllVariablesFromSinks(LPhyParser parser) {
            List<RandomVariable<?>> variables = new ArrayList<>();
            for (Value<?> value : getAllValuesFromSinks(parser)) {
                if (value instanceof RandomVariable<?>) {
                    variables.add((RandomVariable<?>) value);
                }
            }
            return variables;
        }

        public static void parseCommand(Command command, String commandString, LPhyParser parser) {
            commandString = commandString.trim();
            if (!commandString.startsWith(command.getName())) {
                throw new RuntimeException();
            }

            String remainder = commandString.substring(command.getName().length());
            if (remainder.endsWith(";")) {
                remainder = remainder.substring(0, remainder.length() - 1);
            }
            Map<String, Value<?>> arguments =
                    parseArguments(remainder.substring(1, remainder.length() - 1), parser);
            command.execute(arguments);
        }

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
        private static Value parseValueExpression(String valueString, LPhyParser parser, Context context) {

            Value<?> val = parser.getValue(valueString, context);

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


        /**
         * Wraps recursively defined functions into a single graphical model node.
         *
         * @param parser
         */
        public static void wrapExpressionNodes(LPhyParser parser) {

            int wrappedExpressionNodeCount = 0;
            boolean found = false;
            do {
                for (Value value : parser.getModelSinks()) {
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

        public static String getNarrative(LPhyParser parser, Narrative narrative, boolean data, boolean model) {

            Map<String, Integer> nameCounts = new HashMap<>();

            List<Value> dataVisited = new ArrayList<>();
            List<Value> modelVisited = new ArrayList<>();

            StringBuilder builder = new StringBuilder();
            for (Value value : parser.getModelSinks()) {

                Value.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                    @Override
                    public void visitValue(Value value) {

                        if (parser.inDataBlock(value)) {
                            if (!dataVisited.contains(value)) {
                                dataVisited.add(value);

                                String name = NarrativeUtils.getName(value);
                                if (!value.isAnonymous() && !parser.isClamped(value.getId())) {
                                    nameCounts.merge(name, 1, Integer::sum);
                                }
                            }
                        } else {
                            if (!modelVisited.contains(value)) {
                                modelVisited.add(value);
                                String name = NarrativeUtils.getName(value);
                                nameCounts.merge(name, 1, Integer::sum);
                            }
                        }
                    }

                    public void visitGenerator(Generator generator) {
                    }
                }, false);
            }

            if (dataVisited.size() > 0 && data) {
                builder.append(narrative.section("Data"));
                for (Value dataValue : dataVisited) {

                    String name = NarrativeUtils.getName(dataValue);
                    Integer count = nameCounts.get(name);
                    if (count != null) {
                        String valueNarrative = dataValue.getNarrative(count == 1, narrative);
                        builder.append(valueNarrative);
                        if (valueNarrative.length() > 0) builder.append("\n");
                    } else {
                        LoggerUtils.log.severe("No name count found for " + dataValue + " with name " + name);
                    }

                }
                builder.append("\n\n");
            }
            if (modelVisited.size() > 0 && model) {
                builder.append(narrative.section("Model"));

                for (Value modelValue : modelVisited) {

                    String name = NarrativeUtils.getName(modelValue);
                    Integer count = nameCounts.get(name);

                    if (count != null) {
                        String valueNarrative = modelValue.getNarrative(count == 1, narrative);
                        builder.append(valueNarrative);
                        if (valueNarrative.length() > 0) builder.append("\n");
                    } else {
                        LoggerUtils.log.severe("No name count found for " + modelValue + " with name " + name);
                    }
                }
                builder.append("\n");
            }

            return builder.toString();
        }

        public static String getReferences(GraphicalLPhyParser parser, Narrative narrative) {

            List<Citation> refs = new ArrayList<>();
            for (Value value : parser.getModelSinks()) {

                Value.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                    @Override
                    public void visitValue(Value value) {
                    }

                    public void visitGenerator(Generator generator) {
                        Citation citation = generator.getCitation();
                        if (citation != null && !refs.contains(citation)) {
                            refs.add(citation);
                        }
                    }
                }, false);
            }

            return narrative.referenceSection();
        }


        public static String getInferenceStatement(LPhyParser parser, Narrative narrative) {

            List<Value> modelVisited = new ArrayList<>();
            List<Value> dataValues = new ArrayList<>();

            StringBuilder builder = new StringBuilder();
            for (Value value : parser.getModelSinks()) {

                Value.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                    @Override
                    public void visitValue(Value value) {

                        if (!parser.isNamedDataValue(value)) {
                            if (!modelVisited.contains(value)) {
                                modelVisited.add(value);
                                if (parser.isClamped(value.getId()) || value.getOutputs().size() == 0) {
                                    dataValues.add(value);
                                }
                            }
                        }
                    }

                    public void visitGenerator(Generator generator) {
                    }
                }, false);
            }

            if (modelVisited.size() > 0) {

                builder.append(narrative.startMathMode(false));

                builder.append("P(");
                int count = 0;
                for (Value modelValue : modelVisited) {
                    if (!dataValues.contains(modelValue) && modelValue instanceof RandomVariable) {
                        if (count > 0) builder.append(", ");
                        String name = narrative.getId(modelValue, false);
                        builder.append(name);
                        count += 1;
                    }
                }
                if (dataValues.size() > 0) builder.append(" | ");
                count = 0;
                for (Value dataValue : dataValues) {

                    String name = narrative.getId(dataValue, false);
                    if (count > 0 && name != null) builder.append(", ");

                    if (name != null) builder.append(name);
                    count += 1;
                }
                builder.append(") ");
                builder.append(narrative.symbol("∝"));
                builder.append(" ");


                for (Value modelValue : modelVisited) {
                    if (modelValue instanceof RandomVariable) {
                        String statement = modelValue.getGenerator().getInferenceStatement(modelValue, narrative);
                        builder.append(statement);
                        builder.append(" ");
                    }
                }

                builder.append(narrative.endMathMode());

                builder.append("\n");
            }

            return builder.toString();
        }


        public static List<Value<?>> getAllValuesFromSinks(LPhyParser parser) {
            List<Value<?>> values = new ArrayList<>();
            for (Value<?> v : parser.getModelSinks()) {
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

    /**
     * @param value
     * @return true if this is a named value in the data block.
     */
    default boolean isNamedDataValue(Value value) {
        return (!value.isAnonymous() && !(value instanceof RandomVariable) && hasValue(value.getId(), Context.data));
    }

    default boolean inDataBlock(Value value) {
        return getDataValues().contains(value);
    }
}