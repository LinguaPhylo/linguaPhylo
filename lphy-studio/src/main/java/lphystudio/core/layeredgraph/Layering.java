package lphystudio.core.layeredgraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public interface Layering {

    void apply(List<LayeredNode> nodes);

    class LongestPathFromSinks implements Layering {

        @Override
        public void apply(List<LayeredNode> nodes) {

            for (LayeredNode node : nodes) {
                node.setLayer(0);
            }

            int[] minLayer = {0};
            for (LayeredNode sink : getSinks(nodes)) {
                traverseToSetLayers(sink, 0, minLayer);
            }

            for (LayeredNode node : nodes) {
                node.setLayer(node.getLayer()-minLayer[0]);
            }
        }

        private void traverseToSetLayers(LayeredNode node, int layer, int[] minLayer) {

            if (node.getLayer() > layer) {
                node.setLayer(layer);
                if (layer < minLayer[0]) {
                    minLayer[0] = layer;
                }
            }
            for (LayeredNode child : node.getPredecessors()) {
                traverseToSetLayers(child, layer - 1, minLayer);
            }
        }

        public String toString() {
            return "From Sinks";
        }
    }

    class LongestPathFromSources implements Layering {

        int MAX_LAYERS = 20;

        @Override
        public void apply(List<LayeredNode> layeredNodes) {
            List<LayeredNode> nodes = new LinkedList<>();
            nodes.addAll(layeredNodes);
            List<LayeredNode> predecessors = getSources(nodes);

            nodes.removeAll(predecessors); // nodes now contains only nodes that are not roots;
            setLayer(predecessors, 0);
            for (int layerIndex = 1; !nodes.isEmpty(); layerIndex++) {
                if (layerIndex > MAX_LAYERS)
                    throw new RuntimeException(
                            "Graphical tree exceeds maximum depth of " + MAX_LAYERS
                                    + "! (Graph not directed? Cycles?)");
                List<LayeredNode> layer = new ArrayList<>();
                for (LayeredNode item : nodes) {
                    if (predecessors.containsAll(item.getPredecessors()))
                        layer.add(item);
                }
                nodes.removeAll(layer);
                predecessors.addAll(layer);
                setLayer(layer, layerIndex);
            }
        }

        public String toString() {
            return "From Sources";
        }

    }

    static List<LayeredNode> getSinks(List<LayeredNode> nodes) {
        List<LayeredNode> sinks = new ArrayList<>();
        for (LayeredNode node : nodes) {
            if (node.isSink()) {
                sinks.add(node);
            }
        }
        return sinks;
    }

    /**
     * @param nodes
     * @return the nodes that have no incoming edges
     */
    static List<LayeredNode> getSources(List<LayeredNode> nodes) {
        List<LayeredNode> sources = new ArrayList<>();
        for (LayeredNode node : nodes) {
            if (node.isSource()) {
                sources.add(node);
            }
        }
        return sources;
    }

    static void setLayer(List<LayeredNode> nodes, int layer) {
        for (LayeredNode node : nodes) {
            node.setLayer(layer);
        }
    }
}
