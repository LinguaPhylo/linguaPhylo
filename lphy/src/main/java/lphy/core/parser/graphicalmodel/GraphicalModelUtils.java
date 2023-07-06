package lphy.core.parser.graphicalmodel;

import lphy.core.model.ExpressionNode;
import lphy.core.model.Generator;
import lphy.core.model.GraphicalModelNode;
import lphy.core.model.Value;
import lphy.core.parser.function.ExpressionNodeWrapper;

import java.util.ArrayList;
import java.util.List;

public class GraphicalModelUtils {


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

    public static List<Value> getAllValuesFromSinks(GraphicalModel model) {
        List<Value> values = new ArrayList<>();
        for (Value<?> v : model.getModelSinks()) {
            getAllValues(v, values);
        }
        return values;
    }

    private static void getAllValues(GraphicalModelNode<?> node, List<Value> values) {
        if (node instanceof Value && !values.contains(node)) {
            values.add((Value<?>) node);
        }
        for (GraphicalModelNode<?> childNode : node.getInputs()) {
            getAllValues(childNode, values);
        }
    }
}
