package james.core;

import james.graphicalModel.*;
import james.parser.ExpressionNode;
import james.parser.ExpressionNodeWrapper;
import james.utils.LoggerUtils;

import java.util.*;

public interface LPhyParser {

    /**
     * @return the dictionary of parsed values with id's, keyed by id
     */
    Map<String, Value<?>> getDictionary();

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
        keywords.addAll(getGenerativeDistributionClasses().keySet());
        return keywords;
    }

    void parse(String code);

    /**
     * @return the classes of generative distributions recognised by this parser, keyed by their name in lphy
     */
    Map<String, Set<Class<?>>> getGenerativeDistributionClasses();

    List<String> getLines();

    /**
     * Clears the dictionary and lines of this parser.
     */
    void clear();

    class Utils {

        /**
         * Wraps recursively defined functions into a single graphical model node.
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
            LoggerUtils.log.fine("Wrapped " + wrappedExpressionNodeCount + " expression subtrees.");
        }

        private static boolean wrapExpressionNodes(Value value) {
            for (GraphicalModelNode node : (List<GraphicalModelNode>)value.getInputs()) {
                if (node instanceof ExpressionNode) {
                    ExpressionNode eNode = (ExpressionNode)node;

                    if (ExpressionNodeWrapper.expressionSubtreeSize(eNode) > 1) {
                        LoggerUtils.log.info("  Wrapped " + node + ".");
                        ExpressionNodeWrapper wrapper = new ExpressionNodeWrapper((ExpressionNode) node);
                        value.setFunction(wrapper);
                        return true;
                    }
                }
            }
            for (GraphicalModelNode node : (List<GraphicalModelNode>)value.getInputs()) {
                if (node instanceof Generator) {
                    Generator p = (Generator) node;
                    for (GraphicalModelNode v : (List<GraphicalModelNode>)p.getInputs()) {
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

                    public void visitGenerator(Generator generator) {}
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