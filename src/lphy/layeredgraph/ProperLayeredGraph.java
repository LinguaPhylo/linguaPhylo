package lphy.layeredgraph;

import java.util.List;
import java.util.*;

/**
 * The SugiyamaLayoutAlgorithm class implements an algorithm to arrange a
 * directed graph in a layered tree-like layout. The final presentation follows
 * five design principles for enhanced readability:
 * <p>
 * - Hierarchical layout of vertices - Least crossings of lines (edges) -
 * Straightness of lines when ever possible - Close layout of vertices connected
 * to each other, i.e. short paths - Balanced layout of lines coming into or
 * going from a vertex
 * <p>
 * For further information see http://dx.doi.org/10.1109/TSMC.1981.4308636
 *
 * @author Rene Kuhlemann
 * @version 1.2
 */
public class ProperLayeredGraph extends LayeredGraph {

    Layering layering;
    LayeredGraph wrappedGraph;

    final Map<LayeredNode, NodeWrapper> map = new IdentityHashMap<>();

    public ProperLayeredGraph(LayeredGraph wrappedGraph, Layering layering) {

        this.layering = layering;
        this.wrappedGraph = wrappedGraph;

        setup();
    }

    public void setWrappedGraph(LayeredGraph layeredGraph) {

        this.wrappedGraph = layeredGraph;
        setup();
    }

    void setup() {
        makeProper();
    }

    final void makeProper() {
        layers.clear();
        map.clear();
        wrappedGraph.applyLayering(layering);
        int i = 0;
        for (List<LayeredNode> layer : wrappedGraph.layers) {
            addLayer(layer);
        }
        updateIndex();
    }

    public List<LayeredNode> getNodes() {
        ArrayList<LayeredNode> nodes = new ArrayList<>();
        for (List<LayeredNode> layer : layers) {
            nodes.addAll(layer);
        }
        return nodes;
    }

    /**
     * Wraps all {@link LayeredNode} objects into an internal presentation
     * {@link NodeWrapper} and inserts dummy wrappers into the layers between an
     * object and their predecessing nodes if necessary. Finally, all nodes are
     * chained over immediate adjacent layers down to their predecessors.
     *
     * @param list : List of all {@link LayeredNode} objects within the current
     *             layer
     */
    private void addLayer(List<LayeredNode> list) {
        List<LayeredNode> layer = new ArrayList<>(list.size());
        for (LayeredNode node : list) {
            // wrap each NodeLayout with the internal data object and provide a
            // corresponding mapping
            NodeWrapper nw = new NodeWrapper(node, layers.size());
            map.put(node, nw);
            layer.add(nw);
            // insert dummy nodes if the adjacent layer does not contain the
            // predecessor
            for (LayeredNode node_predecessor : node.getPredecessors()) { // for
                // all
                // predecessors
                LayeredNode nw_predecessor = map.get(node_predecessor);
                if (nw_predecessor == null) {
                    throw new RuntimeException("Node wrapped Predecessor of node " + node + " was null!");
                }

                for (int layerIndex = nw_predecessor.getLayer() + 1; layerIndex < nw.getLayer(); layerIndex++) {
                    // add "virtual" wrappers (dummies) to the layers in between
                    // virtual wrappers are in fact parts of a double linked
                    // list
                    NodeWrapper nw_dummy = new NodeWrapper(layerIndex);
                    nw_dummy.getPredecessors().add(nw_predecessor);
                    nw_predecessor.getSuccessors().add(nw_dummy);
                    nw_predecessor = nw_dummy;
                    layers.get(layerIndex).add(nw_dummy);
                }
                nw.getPredecessors().add(nw_predecessor);
                nw_predecessor.getSuccessors().add(nw);
            }
        }
        layers.add(layer);
        updateIndex(layers.size()-1);
    }
}
