package lphystudio.core.layeredgraph;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;
import lphy.layeredgraph.LayeredGraph;
import lphy.layeredgraph.LayeredNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayeredGraphFactory {

    public static LayeredGraph createLayeredGraph(LPhyParser parser, boolean showAllNodes) {
        Map<Object, LayeredGNode> allNodes = new HashMap<>();
        for (Value value : parser.getModelSinks()) {
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

        if (value == null) {
            throw new RuntimeException("Value is null!");
        }

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
                Generator child = value.getGenerator();

                if (child != null) {
                    LayeredGNode childNode = createAndAddNode(parser, child, node, allNodes, showAllNodes);
                    if (childNode != null) node.getPredecessors().add(childNode);
                }
            }
        }

        return node;
    }

    private static LayeredGNode createAndAddNode(LPhyParser parser, Generator g, LayeredGNode parentNode, Map<Object, LayeredGNode> allNodes, boolean showAllNodes) {
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
