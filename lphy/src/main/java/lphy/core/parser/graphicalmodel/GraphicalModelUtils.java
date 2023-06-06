package lphy.core.parser.graphicalmodel;

import lphy.core.exception.LoggerUtils;
import lphy.core.model.Generator;
import lphy.core.model.GraphicalModelNode;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.narrative.Narrative;
import lphy.core.narrative.NarrativeUtils;
import lphy.core.parser.function.ExpressionNode;
import lphy.core.parser.function.ExpressionNodeWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static lphy.core.narrative.CitationUtils.getCitation;

public class GraphicalModelUtils {


    private static int wrapLength = 80;

    /**
     * @param line a line of LPhy code
     * @return true if the line of code is declaring a random variable.
     */
    public static boolean isRandomVariableLine(String line) {
        return (line.indexOf('~') > 0);
    }

    /**
     * Wraps recursively defined functions into a single graphical model node.
     *
     * @param model
     */
    public static void wrapExpressionNodes(GraphicalModel model) {

        int wrappedExpressionNodeCount = 0;
        boolean found = false;
        do {
            for (Value value : model.getModelSinks()) {
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

    public static String getNarrative(GraphicalModel model, Narrative narrative, boolean data, boolean includeModelBlock) {

        Map<String, Integer> nameCounts = new HashMap<>();

        List<Value> dataVisited = new ArrayList<>();
        List<Value> modelVisited = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        for (Value value : model.getModelSinks()) {

            Value.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                @Override
                public void visitValue(Value value) {

                    if (model.inDataBlock(value)) {
                        if (!dataVisited.contains(value)) {
                            dataVisited.add(value);

                            String name = NarrativeUtils.getName(value);
                            if (!value.isAnonymous() && !model.isClamped(value.getId())) {
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
                    //TODO need log4j or similar to disable debug messages after release
//                        LoggerUtils.log.info("No name count found for " + dataValue + " with name " + name);
                }

            }
            builder.append("\n\n");
        }
        if (modelVisited.size() > 0 && includeModelBlock) {
            builder.append(narrative.section("Model"));

            for (Value modelValue : modelVisited) {

                String name = NarrativeUtils.getName(modelValue);
                Integer count = nameCounts.get(name);

                if (count != null) {
                    String valueNarrative = modelValue.getNarrative(count == 1, narrative);
                    builder.append(valueNarrative);
                    if (valueNarrative.length() > 0) builder.append("\n");
                } else {
                    LoggerUtils.log.info("No name count found for " + modelValue + " with name " + name);
                }
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public static String getReferences(GraphicalModel model, Narrative narrative) {

        List<Citation> refs = new ArrayList<>();
        for (Value value : model.getModelSinks()) {

            Value.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                @Override
                public void visitValue(Value value) {
                }

                public void visitGenerator(Generator generator) {
                    Citation citation = getCitation(generator.getClass());
                    if (citation != null && !refs.contains(citation)) {
                        refs.add(citation);
                    }
                }
            }, false);
        }

        return narrative.referenceSection();
    }

    public static String getInferenceStatement(GraphicalModel model, Narrative narrative) {

        List<Value> modelVisited = new ArrayList<>();
        List<Value> dataValues = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        for (Value value : model.getModelSinks()) {

            Value.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                @Override
                public void visitValue(Value value) {

                    if (!model.isNamedDataValue(value)) {
                        if (!modelVisited.contains(value)) {
                            modelVisited.add(value);
                            if (model.isClamped(value.getId()) || value.getOutputs().size() == 0) {
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

            builder.append(narrative.startMathMode(false, true));

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
            builder.append(narrative.mathAlign());


            int currentLineLength = 0;

            List<RandomVariable> randomVariables = modelVisited.stream().filter(value -> value instanceof RandomVariable).map(value -> (RandomVariable) value).collect(Collectors.toList());

            for (int i = 0; i < randomVariables.size(); i++) {
                RandomVariable modelVariable = randomVariables.get(i);
                String statement = modelVariable.getGenerator().getInferenceStatement(modelVariable, narrative);
                builder.append(statement);
                currentLineLength += statement.length();

                if (currentLineLength > wrapLength && i < modelVisited.size() - 1) {
                    builder.append(narrative.mathNewLine());
                    builder.append(narrative.mathAlign());
                    currentLineLength = 0;
                    builder.append(" ");
                }
            }

            builder.append(narrative.endMathMode());


            builder.append("\n");
        }

        return builder.toString();
    }

    public static List<Value<?>> getAllValuesFromSinks(GraphicalModel model) {
        List<Value<?>> values = new ArrayList<>();
        for (Value<?> v : model.getModelSinks()) {
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
