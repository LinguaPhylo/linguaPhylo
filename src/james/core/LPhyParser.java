package james.core;

import james.graphicalModel.*;

import java.util.*;
import java.util.stream.Collectors;

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

    class Utils {

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

                    public void visitGenDist(GenerativeDistribution genDist) {}

                    public void visitFunction(DeterministicFunction f) {}
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