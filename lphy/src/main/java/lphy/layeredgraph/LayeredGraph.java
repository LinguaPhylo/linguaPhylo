package lphy.layeredgraph;

import java.util.ArrayList;
import java.util.Arrays;
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

    public LayeredGraph(List<List<LayeredNode>> layers) {
        this.layers = layers;

        for (List<LayeredNode> layer : layers) {
            nodes.addAll(layer);
        }
    }

    public void applyLayering(Layering layering) {
        layering.apply(nodes);
        repopulateLayers();
    }

    public List<LayeredNode> getNodes() {
        return nodes;
    }

    public List<List<LayeredNode>> getLayers() {
        return layers;
    }

    public void addNode(LayeredNode node) {
        nodes.add(node);
        addNodeToLayers(node);
    }

    public int getLayerCount() {
        return layers.size();
    }

    /**
     * @return the maximum x index across all the layers.
     */
    public int getMaxIndex() {
        int maxIndex = 0;
        for (LayeredNode v : getNodes()) {
            if (v.getIndex() > maxIndex) maxIndex = v.getIndex();
        }
        return maxIndex;
    }

    public double getMaxX() {
        double maxX = 0;
        for (LayeredNode v : getNodes()) {
            if (v.getX() > maxX) maxX = v.getX();
        }
        return maxX;
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

    public void updateIndex() {
        for (List<LayeredNode> layer : layers) {
            updateIndex(layer);
        }
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
        for (List<LayeredNode> layer : layers) {
            updateIndex(layer);
        }

    }

    void updateIndex(int layerIndex) {
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

    public static LayeredGraph testGraph() {

        List<List<LayeredNode>> layers = new ArrayList<>();

        LayeredNode.Default v00 = new LayeredNode.Default(0,0);
        LayeredNode.Default v01 = new LayeredNode.Default(0,1);
        layers.add(Arrays.asList(v00, v01));

        LayeredNode.Default v10 = new LayeredNode.Default(1,0);
        LayeredNode.Default v11 = new LayeredNode.Default(1,1);
        LayeredNode.Default d12 = new LayeredNode.Dummy(1,2);
        LayeredNode.Default v13 = new LayeredNode.Default(1,3);
        LayeredNode.Default d14 = new LayeredNode.Dummy(1,4);
        LayeredNode.Default d15 = new LayeredNode.Dummy(1,5);
        LayeredNode.Default v16 = new LayeredNode.Default(1,6);
        LayeredNode.Default v17 = new LayeredNode.Default(1,7);
        layers.add(Arrays.asList(v10, v11, d12, v13, d14, d15, v16, v17));

        LayeredNode.Default v20 = new LayeredNode.Default(2,0);
        LayeredNode.Default v21 = new LayeredNode.Default(2,1);
        LayeredNode.Default d22 = new LayeredNode.Dummy(2,2);
        LayeredNode.Default d23 = new LayeredNode.Dummy(2,3);
        LayeredNode.Default d24 = new LayeredNode.Dummy(2,4);
        LayeredNode.Default v25 = new LayeredNode.Default(2,5);
        layers.add(Arrays.asList(v20, v21, d22, d23, d24, v25));

        LayeredNode.Default v30 = new LayeredNode.Default(3,0);
        LayeredNode.Default v31 = new LayeredNode.Default(3,1);
        LayeredNode.Default d32 = new LayeredNode.Dummy(3,2);
        LayeredNode.Default d33 = new LayeredNode.Dummy(3,3);
        LayeredNode.Default d34 = new LayeredNode.Dummy(3,4);
        LayeredNode.Default v35 = new LayeredNode.Default(3,5);
        LayeredNode.Default d36 = new LayeredNode.Dummy(3,6);
        layers.add(Arrays.asList(v30, v31, d32, d33, d34, v35, d36));

        LayeredNode v40 = new LayeredNode.Default(4,0);
        LayeredNode v41 = new LayeredNode.Default(4,1);
        LayeredNode v42 = new LayeredNode.Default(4,2);
        layers.add(Arrays.asList(v40, v41, v42));

        v00.addSuccessor(v10); // v(0,0) -> v(1,0)
        v00.addSuccessor(d15); // v(0,0) -> d(1,5)
        v00.addSuccessor(v17); // v(0,0) -> v(1,7)
        v01.addSuccessor(d12); // v(0,1) -> d(1,2)
        v01.addSuccessor(d14);// v(0,1) -> d(1,4)
        v11.addSuccessor(v21);// v(1,1) -> v(2,1)
        d12.addSuccessor(v21);// d(1,2) -> v(2,1)
        v13.addSuccessor(v21);// v(1,3) -> v(2,1)
        d14.addSuccessor(d22);// d(1,4) -> d(2,2)
        d15.addSuccessor(d23);// d(1,5) -> d(2,3)
        v16.addSuccessor(v25);// v(1,6) -> v(2,5)
        v17.addSuccessor(d24);// v(1,7) -> d(2,4)
        v20.addSuccessor(v30);// v(2,0) -> v(3,0)
        v20.addSuccessor(v31);// v(2,0) -> v(3,1)
        v20.addSuccessor(v35);// v(2,0) -> v(3,5)
        d22.addSuccessor(d33);// d(2,2) -> d(3,3)
        d23.addSuccessor(d34);// d(2,3) -> d(3,4)
        d24.addSuccessor(v35);// d(2,4) -> v(3,5)
        v25.addSuccessor(d32);// v(2,5) -> d(3,6)
        v25.addSuccessor(d36);// v(2,5) -> d(3,6)
        v30.addSuccessor(v40);// v(3,0) -> v(4,0)
        v30.addSuccessor(v41);// v(3,0) -> v(4,1)
        v31.addSuccessor(v41);// v(3,1) -> v(4,1)
        d32.addSuccessor(v40);// d(3,2) -> v(4,0)
        d33.addSuccessor(v42);// d(3,3) -> v(4,2)
        d34.addSuccessor(v42);// d(3,4) -> v(4,2)
        v35.addSuccessor(v42);// v(3,5) -> v(4,2)
        d36.addSuccessor(v42);// d(3,6) -> v(4,2)

        return new LayeredGraph(layers);

    }
}
