package james.app.graphicalmodelcomponent;

import james.core.distributions.Utils;

import java.awt.*;
import java.util.*;
import java.util.List;

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
public class SugiyamaLayeredGraph extends LayeredGraph {

    // Tree direction constants
    public final static int HORIZONTAL = 1;
    public final static int VERTICAL = 2;

    // Internal constants
    private static final int MAX_SWEEPS = 35;

    private final Map<LayeredNode, NodeWrapper> map = new IdentityHashMap<>();
    private final int direction;
    private final Dimension dimension;
    private LayeredGraph layeredGraph;
    private Layering layering;

    private int last; // index of the last element in a layer after padding
    // process

    /**
     * Constructs a tree-like, layered layout of a directed graph.
     *
     * @param dir - {@link SugiyamaLayeredGraph#HORIZONTAL}: left to right -
     *            {@link SugiyamaLayeredGraph#VERTICAL}: top to bottom
     * @param dim - desired size of the layout area.
     */
    public SugiyamaLayeredGraph(int dir, Dimension dim, Layering layering) {
        if (dir == HORIZONTAL)
            direction = HORIZONTAL;
        else
            direction = VERTICAL;
        dimension = dim;
        this.layering = layering;
    }

    public void setLayeredGraph(LayeredGraph layeredGraph) {
        this.layeredGraph = layeredGraph;
    }

    /*
     * applyLayout
     */
    public void applyLayout(boolean clean) {


        if (!clean)
            return;
        layers.clear();
        map.clear();
        createLayers();
        padLayers();
        for (int i = 0; i < layers.size(); i++) { // reduce and refine
            // iteratively, depending on
            // the depth of the graph
            reduceCrossings();
            refineLayers();
        }
        reduceCrossings();

        for (int i = 0; i < layers.size(); i++) {
            int length = layers.get(i).size();
            for (int j = 0; j < layers.get(i).size(); j++) {
                NodeWrapper nw = (NodeWrapper) layers.get(i).get(j);
                nw.setX(nw.getIndex());
            }
        }

//        removePadding();
//
//        int widest = 0;
//        for (int i = 0; i < layers.size(); i++) {
//            int length = layers.get(i).size();
//            if (length > widest) widest = length;
//        }
//
//        for (int i = 0; i < layers.size(); i++) {
//            int length = layers.get(i).size();
//            for (int j = 0; j < layers.get(i).size(); j++) {
//                NodeWrapper nw = (NodeWrapper) layers.get(i).get(j);
//
//                double equalSpaceX = ((double)j+0.5)/(double)length*(double)widest;
//
//                nw.setX(equalSpaceX);
//            }
//        }
//
//        for (LayeredNode node : getNodes()) {
//            if (node.isSink()) {
//                System.out.println("Sink: index = " + node.getIndex() + "; " + node);
//            }
//        }
//
//        relax(1000);

        calculatePositions();
    }

    private void createLayers() {
        layeredGraph.applyLayering(layering);
        int i = 0;
        for (List<LayeredNode> layer : layeredGraph.layers) {
            System.out.println("Layer " + i + ": " + layer);
            i += 1;
        }
        for (List<LayeredNode> layer : layeredGraph.layers) {
            addLayer(layer);
        }
    }

    private void removePadding() {

        for (int i = 0; i < layers.size(); i++) {
            List<LayeredNode> toRemove = new ArrayList<>();
            for (LayeredNode n : layers.get(i)) {
                if (((NodeWrapper)n).isPadding()) toRemove.add(n);
            }
            layers.get(i).removeAll(toRemove);
            updateIndex(i);
        }
    }

    public List<LayeredNode> getNodes() {
        ArrayList<LayeredNode> nodes = new ArrayList<>();
        for (List<LayeredNode> layer : layers) {
            nodes.addAll(layer);
        }
        return nodes;
    }

    public void relax(int reps) {

        int nodeCount = getNodes().size();

        if (nodeCount > 0) {
            for (int i = 0; i < reps; i++) {

                int nodeIndex = Utils.getRandom().nextInt(nodeCount);
                LayeredNode node = getNodes().get(nodeIndex);

                if (node.getLayer() > 0 && !node.isSink()) {
                    double x = node.getX();
                    double nx = node.getXBarycenter(true, true);

                    //System.out.println("x = " + x + "; nx = " + nx);

                    node.setX(nx);
                }
            }
        }
    }

    private void getXByBaryCentre() {

        if (layers.size() > 0) {
            for (int i = 0; i < layers.get(0).size(); i++) {
                LayeredNode nw = layers.get(0).get(i);
                nw.setX(i);
            }

            // remove padding
            for (int index = 1; index < layers.size(); index++) {
                refineXLayersDown(layers.get(index));
            }
        }
    }

    private void refineXLayersDown(List<LayeredNode> layer) {

        // second, remove padding from the layer's end and place them in front
        // of the current node to improve its position
        for (LayeredNode node : layer) {
            node.setX(((NodeWrapper)node).getXBaryCenter(node.getPredecessors())); // distance
        }
    }

    /**
     * Wraps all {@link LayeredNode} objects into an internal presentation
     * {@link NodeWrapper} and inserts dummy wrappers into the layers between an
     * object and their predecessing nodes if necessary. Finally, all nodes are
     * chained over immediate adjacent layers down to their predecessors. This
     * is necessary to apply the final step of the Sugiyama algorithm to refine
     * the node position within a layer.
     *
     * @param list : List of all {@link LayeredNode} objects within the current
     *             layer
     */
    private void addLayer(List<LayeredNode> list) {
        ArrayList<LayeredNode> layer = new ArrayList<>(list.size());
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
                NodeWrapper nw_predecessor = map.get(node_predecessor);
                if (nw_predecessor == null) {
                    throw new RuntimeException("Node wrapped Predecessor of node " + node + " was null!");
                }

                for (int layerIndex = nw_predecessor.getLayer() + 1; layerIndex < nw.getLayer(); layerIndex++) {
                    // add "virtual" wrappers (dummies) to the layers in between
                    // virtual wrappers are in fact parts of a double linked
                    // list
                    NodeWrapper nw_dummy = new NodeWrapper(layerIndex);
                    nw_dummy.addPredecessor(nw_predecessor);
                    nw_predecessor.addSuccessor(nw_dummy);
                    nw_predecessor = nw_dummy;
                    layers.get(layerIndex).add(nw_dummy);
                }
                nw.addPredecessor(nw_predecessor);
                nw_predecessor.addSuccessor(nw);
            }
        }
        layers.add(layer);
        updateIndex(layers.size()-1);
    }

    /**
     * Reduces connection crossings between two adjacent layers by a combined
     * top-down and bottom-up approach. It uses a heuristic approach based on
     * the predecessor's barycenter.
     */
    private void reduceCrossings() {
        for (int round = 0; round < MAX_SWEEPS; round++) {
            if ((round & 1) == 0) { // if round is even then do a bottom-up scan
                for (int index = 1; index < layers.size(); index++)
                    reduceCrossingsDown(layers.get(index));
            } else { // else top-down
                for (int index = layers.size() - 2; index >= 0; index--) {
                    reduceCrossingsUp(layers.get(index));
                }
            }
        }
    }

    private static void reduceCrossingsDown(List<LayeredNode> layer) {
        for (LayeredNode node : layer) {
            node.setIndex(((NodeWrapper)node).getBaryCenter(node.getPredecessors()));
        }
        layer.sort(Comparator.comparingDouble(LayeredNode::getIndex));
        updateIndex(layer);
    }

    private static void reduceCrossingsUp(List<LayeredNode> layer) {
        for (LayeredNode node : layer) {
            node.setIndex(((NodeWrapper)node).getBaryCenter(node.getSuccessors()));
        }
        layer.sort(Comparator.comparingDouble(LayeredNode::getIndex));
        updateIndex(layer);
    }

    /**
     * Fills in virtual nodes, so the layer system finally becomes an
     * equidistant grid
     */
    private void padLayers() {
        last = 0;
        for (List<LayeredNode> layer : layers) {
            if (layer.size() > last) {
                last = layer.size();
            }
        }
        last--; // index of the last element of any layer
        for (List<LayeredNode> layer : layers) { // padding is always added at
            // the END of each layer!
            for (int i = layer.size(); i <= last; i++)
                layer.add(new NodeWrapper());
            updateIndex(layer);
        }
    }

    private void refineLayers() { // from Sugiyama paper: down, up and down
        // again yields best results, wonder why...
        for (int index = 1; index < layers.size(); index++)
            refineLayersDown(layers.get(index));
        for (int index = layers.size() - 2; index >= 0; index--)
            refineLayersUp(layers.get(index));
        for (int index = 1; index < layers.size(); index++)
            refineLayersDown(layers.get(index));
    }

    private void refineLayersDown(List<LayeredNode> layer) {
        // first, get a priority list
        List<NodeWrapper> list = new ArrayList<>();
        layer.forEach((node) -> list.add((NodeWrapper)node));
        Collections.sort(list, (node1, node2) -> {
            return (node2.getPriorityDown() - node1.getPriorityDown()); // descending
            // ordering!!!
        });
        // second, remove padding from the layer's end and place them in front
        // of the current node to improve its position
        for (NodeWrapper node : list) {
            if (node.isPadding())
                break; // break, if there are no more "real" nodes
            int delta = node.getBaryCenter(node.getPredecessors()) - node.getIndex(); // distance
            // to new
            // position
            for (int i = 0; i < delta; i++)
                layer.add(node.getIndex(), layer.remove(last));
        }
        updateIndex(layer);
    }

    private void refineLayersUp(List<LayeredNode> layer) {
        // first, get a priority list
        List<NodeWrapper> list = new ArrayList<>();
        layer.forEach((node) -> list.add((NodeWrapper)node));

        Collections.sort(list, (node1, node2) -> {
            return (node2.getPriorityUp() - node1.getPriorityUp()); // descending
            // ordering!!!
        });
        // second, remove padding from the layer's end and place them in front
        // of the current node to improve its position
        for (NodeWrapper node : list) {
            if (node.isPadding())
                break; // break, if there are no more "real" nodes
            int delta = node.getBaryCenter(node.getSuccessors()) - node.getIndex(); // distance
            // to new
            // position
            for (int i = 0; i < delta; i++) {
                layer.add(node.getIndex(), layer.remove(last));
            }
        }
        updateIndex(layer);
    }

    private void calculatePositions() {

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        for (int i = 0; i < layers.size(); i++) {
            for (LayeredNode n : layers.get(i)) {
                if (n.getX() > maxX) maxX = n.getX();
                if (n.getX() < minX) minX = n.getX();
            }
        }

        double dx = dimension.getWidth() / layers.size();
        double dy = dimension.getHeight() / ((maxX-minX) + 1);

        if (direction == VERTICAL) {
            dx = dimension.getWidth() / ((maxX-minX) + 1);
            dy = dimension.getHeight() / layers.size();
        }

        if (direction == HORIZONTAL)
            for (LayeredNode node : layeredGraph.getNodes()) {
                NodeWrapper nw = map.get(node);
                node.setPosition((nw.getLayer() + 0.5d) * dx, (nw.getX()-minX + 0.5d) * dy);
            }
        else
            for (LayeredNode node : layeredGraph.getNodes()) {
                NodeWrapper nw = map.get(node);
                if (nw == null) throw new RuntimeException("Couldn't find node wrapper for node " + node);
                node.setPosition((nw.getX()-minX + 0.5d) * dx, (nw.getLayer() + 0.5d) * dy);
            }
    }
}
