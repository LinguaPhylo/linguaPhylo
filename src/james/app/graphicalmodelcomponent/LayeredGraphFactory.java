package james.app.graphicalmodelcomponent;

import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.Parameterized;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import java.util.*;

public class LayeredGraphFactory {

    public static LayeredGraph createLayeredGraph(GraphicalModelParser parser, boolean showAllNodes) {
        Map<Object, LayeredGNode> allNodes = new HashMap<>();
        for (Value value : parser.getSinks()) {
            createAndAddNode(value, null, allNodes, showAllNodes);
        }

        List<LayeredNode> nodes = new ArrayList<>();
        nodes.addAll(allNodes.values());

        return new LayeredGraph(nodes);
    }

    /**
     *
     * @param value
     * @param parentNode
     * @param allNodes
     * @return
     */
    private static LayeredNode createAndAddNode(Value value, LayeredGNode parentNode, Map<Object, LayeredGNode> allNodes, boolean showAllNodes) {

        LayeredGNode node = allNodes.get(value);
        boolean newNode = (node == null);

        if (newNode && (value.isRandom() || showAllNodes)) {
            node = new LayeredGNode(value);
        }

        if (node != null) {
            if (parentNode != null && !node.getSuccessors().contains(parentNode)) {
                node.addOutput(parentNode);
            }
            node.setLayer();

            if (newNode) {
                allNodes.put(node.value(), node);
                Parameterized child;
                if (value instanceof RandomVariable) {
                    child = ((RandomVariable) value).getGenerativeDistribution();
                } else {
                    child = value.getFunction();
                }

                if (child != null) {
                    LayeredGNode childNode = createAndAddNode(child, node, allNodes, showAllNodes);
                    if (childNode != null) node.getPredecessors().add(childNode);
                }
            }
        }

        return node;
    }

    private static LayeredGNode createAndAddNode(Parameterized g, LayeredGNode parentNode, Map<Object, LayeredGNode> allNodes, boolean showAllNodes) {
        LayeredGNode node = allNodes.get(g);
        if (node == null) {
            node = new LayeredGNode(g);
            node.addOutput(parentNode);
            node.setLayer();
            allNodes.put(node.value(), node);

            Map<String, Value> params = g.getParams();
            for (String key : params.keySet()) {
                LayeredNode child = createAndAddNode(params.get(key), node, allNodes, showAllNodes);
                if (child != null) node.getPredecessors().add(child);
            }
        }
        return node;
    }
}
