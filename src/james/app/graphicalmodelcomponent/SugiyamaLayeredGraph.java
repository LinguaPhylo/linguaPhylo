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
public class SugiyamaLayeredGraph extends ProperLayeredGraph {

    // Tree direction constants
    public final static int HORIZONTAL = 1;
    public final static int VERTICAL = 2;

    // Internal constants
    private static final int MAX_SWEEPS = 35;

    private final int direction;
    private final Dimension dimension;

    private int last; // index of the last element in a layer after padding
    // process

    /**
     * Constructs a tree-like, layered layout of a directed graph.
     *
     * @param dir - {@link SugiyamaLayeredGraph#HORIZONTAL}: left to right -
     *            {@link SugiyamaLayeredGraph#VERTICAL}: top to bottom
     * @param dim - desired size of the layout area.
     */
    public SugiyamaLayeredGraph(int dir, Dimension dim, LayeredGraph wrappedGraph, Layering layering) {

        super(wrappedGraph, layering);
        if (dir == HORIZONTAL)
            direction = HORIZONTAL;
        else
            direction = VERTICAL;
        dimension = dim;
        calculatePositions();
    }

    /*
     * setup
     */
    public void setup() {

        makeProper();
        padLayers();
        for (int i = 0; i < layers.size(); i++) { // reduce and refine
            // iteratively, depending on
            // the depth of the graph
            reduceCrossings();
            refineLayers();
        }
        reduceCrossings();

        for (int i = 0; i < layers.size(); i++) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                NodeWrapper nw = (NodeWrapper) layers.get(i).get(j);
                nw.setX(nw.getIndex());
            }
        }

        removePadding();


        new BrandesKopfHorizontalCoordinateAssignment(this);

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

    private void refineXLayersDown(List<LayeredNode> layer) {

        // second, remove padding from the layer's end and place them in front
        // of the current node to improve its position
        for (LayeredNode node : layer) {
            node.setX(((NodeWrapper)node).getXBaryCenter(node.getPredecessors())); // distance
        }
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
            node.setIndex(getBarycenter(node, node.getPredecessors()));
        }
        layer.sort(Comparator.comparingDouble(LayeredNode::getIndex));
        updateIndex(layer);
    }

    private static void reduceCrossingsUp(List<LayeredNode> layer) {
        for (LayeredNode node : layer) {
            node.setIndex(getBarycenter(node, node.getSuccessors()));
        }
        layer.sort(Comparator.comparingDouble(LayeredNode::getIndex));
        updateIndex(layer);
    }

    static int getBarycenter(LayeredNode v, List<LayeredNode> list) {
        if (list.isEmpty())
            return (v.getIndex());
        double barycenter = 0.0;
        for (LayeredNode node : list)
            barycenter += node.getIndex();
        return (int) Math.round(barycenter / list.size()); // always rounding off to
        // avoid wrap around in
        // position refining!!!
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
        list.sort((node1, node2) -> {
            return (getPriorityDown(node2) - getPriorityDown(node1)); // descending
            // ordering!!!
        });
        // second, remove padding from the layer's end and place them in front
        // of the current node to improve its position
        for (NodeWrapper node : list) {
            if (node.isPadding())
                break; // break, if there are no more "real" nodes
            int delta = getBarycenter(node, node.getPredecessors()) - node.getIndex(); // distance
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

        list.sort((node1, node2) -> {
            return (getPriorityUp(node2) - getPriorityUp(node1)); // descending
            // ordering!!!
        });
        // second, remove padding from the layer's end and place them in front
        // of the current node to improve its position
        for (NodeWrapper node : list) {
            if (node.isPadding())
                break; // break, if there are no more "real" nodes
            int delta = getBarycenter(node, node.getSuccessors()) - node.getIndex(); // distance
            // to new
            // position
            for (int i = 0; i < delta; i++) {
                layer.add(node.getIndex(), layer.remove(last));
            }
        }
        updateIndex(layer);
    }


    int getPriorityDown(LayeredNode v) {
        if (v.isDummy()) {
            if (v.getSuccessors().get(0).isDummy())
                return (Integer.MAX_VALUE); // part of a straight line
            else
                return (Integer.MAX_VALUE >> 1); // start of a straight line
        }
        return (v.getPredecessors().size());
    }

    int getPriorityUp(LayeredNode v) {
        if (v.isDummy()) {
            if (v.getPredecessors().get(0).isDummy())
                return (Integer.MAX_VALUE); // part of a straight line
            else
                return (Integer.MAX_VALUE >> 1); // start of a straight line
        }
        return (v.getSuccessors().size());
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
            for (LayeredNode node : getNodes()) {
                //NodeWrapper nw = map.get(node);
                node.setPosition((node.getLayer() + 0.5d) * dx, (node.getX()-minX + 0.5d) * dy);
            }
        else
            for (LayeredNode node : getNodes()) {
                //NodeWrapper nw = map.get(node);
                //if (nw == null) throw new RuntimeException("Couldn't find node wrapper for node " + node);
                node.setPosition((node.getX()-minX + 0.5d) * dx, (node.getLayer() + 0.5d) * dy);
            }
    }
}
