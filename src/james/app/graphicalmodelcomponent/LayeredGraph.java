package james.app.graphicalmodelcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LayeredGraph {

    List<LayeredNode> nodes = new ArrayList<>();

    List<List<LayeredNode>> layers = new ArrayList<>();

    public LayeredGraph() {}

    public LayeredGraph(Collection<LayeredNode> nodes) {
        this.nodes.addAll(nodes);
        repopulateLayers();
    }

    public void applyLayering(Layering layering) {
        layering.apply(nodes);
        repopulateLayers();
    }

    public List<LayeredNode> getNodes() {
        return nodes;
    }

    public void addNode(LayeredNode node) {
        nodes.add(node);
        addNodeToLayers(node);
    }

    private void addNodeToLayers(LayeredNode node) {
        while (layers.size() <= node.getLayer()) {
            layers.add(new ArrayList<>());
        }
        List<LayeredNode> layer = layers.get(node.getLayer());
        if (!layer.contains(node)) {
            layer.add(node);
        }
    }

    public List<LayeredNode> getLayer(int layer) {
        return layers.get(layer);
    }

    private int indexInLayer(LayeredNode node) {
        return layers.get(node.getLayer()).indexOf(node);
    }

    /**
     * Use the LayerNode.layer fields to fill up the layer lists from scratch
     */
    public void repopulateLayers() {
        for (List<LayeredNode> layer : layers) {
            layer.clear();
        }
        layers.clear();

        for (LayeredNode node : nodes) {
            addNodeToLayers(node);
        }
    }

    public void updateIndex(int layerIndex) {
        updateIndex(getLayer(layerIndex));
    }

    /**
     * Expects a list representing a layer in this layered graph
     * @param layer
     */
    static void updateIndex(List<? extends LayeredNode> layer) {
        for (int index = 0; index < layer.size(); index++) {
            layer.get(index).setIndex(index);
        }
    }
}
