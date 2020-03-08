package james.app.graphicalmodelcomponent;

import james.core.LPhyParser;
import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.Parameterized;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import java.util.*;

public class LayeredGraphFactory {

    public static LayeredGraph createLayeredGraph(LPhyParser parser, boolean showAllNodes) {
        Map<Object, LayeredGNode> allNodes = new HashMap<>();
        for (Value value : parser.getSinks()) {
            createAndAddNode(parser, value, null, allNodes, showAllNodes);
        }

        List<LayeredNode> nodes = new ArrayList<>();
        nodes.addAll(allNodes.values());

        int maxLayer = 0;
        for (LayeredNode v : allNodes.values()) {
            if (v.getLayer() > maxLayer) maxLayer = v.getLayer();
        }
        for (LayeredNode v : allNodes.values()) {
            v.setLayer(maxLayer-v.getLayer());
        }

        return new LayeredGraph(nodes);
    }

    /**
     *
     * @param value
     * @param parentNode
     * @param allNodes
     * @return
     */
    private static LayeredNode createAndAddNode(LPhyParser parser, Value value, LayeredGNode parentNode,
                                                Map<Object, LayeredGNode> allNodes, boolean showAllNodes) {

        LayeredGNode node = allNodes.get(value);
        boolean newNode = (node == null);

        if (newNode && (value.isRandom() || showAllNodes)) {
            node = new LayeredGNode(value, parser);
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
                    LayeredGNode childNode = createAndAddNode(parser, child, node, allNodes, showAllNodes);
                    if (childNode != null) node.getPredecessors().add(childNode);
                }
            }
        }

        return node;
    }

    private static LayeredGNode createAndAddNode(LPhyParser parser, Parameterized g, LayeredGNode parentNode, Map<Object, LayeredGNode> allNodes, boolean showAllNodes) {
        LayeredGNode node = allNodes.get(g);
        if (node == null) {
            node = new LayeredGNode(g, parser);
            node.addOutput(parentNode);
            node.setLayer();
            allNodes.put(node.value(), node);

            Map<String, Value> params = g.getParams();
            for (String key : params.keySet()) {
                LayeredNode child = createAndAddNode(parser, params.get(key), node, allNodes, showAllNodes);
                if (child != null) node.getPredecessors().add(child);
            }
        }
        return node;
    }
}
