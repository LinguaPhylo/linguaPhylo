package james.app.graphicalmodelcomponent;

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
public class SugiyamaLayoutAlgorithm {

    // Tree direction constants
    public final static int HORIZONTAL = 1;
    public final static int VERTICAL = 2;

    // Internal constants
    private static final int MAX_LAYERS = 20;
    private static final int MAX_SWEEPS = 35;
    private static final int PADDING = -1;

    private class NodeWrapper {
        int index;
        double x;
        double dx = 0.0;
        final int layer;
        final NodeLayout node;
        final List<NodeWrapper> pred = new LinkedList<>();
        final List<NodeWrapper> succ = new LinkedList<>();

        NodeWrapper(NodeLayout n, int l) {
            node = n;
            layer = l;
        } // NodeLayout wrapper

        NodeWrapper(int l) {
            this(null, l);
        } // Dummy to connect two NodeLayout objects

        NodeWrapper() {
            this(null, PADDING);
        } // Padding for final refinement phase

        void addPredecessor(NodeWrapper node) {
            pred.add(node);
        }

        void addSuccessor(NodeWrapper node) {
            succ.add(node);
        }

        boolean isDummy() {
            return ((node == null) && (layer != PADDING));
        }

        boolean isPadding() {
            return ((node == null) && (layer == PADDING));
        }

        int getBaryCenter(List<NodeWrapper> list) {
            if (list.isEmpty())
                return (this.index);
            double barycenter = 0.0;
            for (NodeWrapper node : list)
                barycenter += node.index;
            return (int) Math.round(barycenter / list.size()); // always rounding off to
            // avoid wrap around in
            // position refining!!!
        }

        public double getXBaryCenter(List<NodeWrapper> list) {
            if (list.isEmpty())
                return (this.x);
            double barycenter = 0.0;
            for (NodeWrapper node : list)
                barycenter += node.x;
            return barycenter / list.size(); // always rounding off to
            // avoid wrap around in
            // position refining!!!
        }


        int getPriorityDown() {
            if (isPadding())
                return (0);
            if (isDummy()) {
                if (succ.get(0).isDummy())
                    return (Integer.MAX_VALUE); // part of a straight line
                else
                    return (Integer.MAX_VALUE >> 1); // start of a straight line
            }
            return (pred.size());
        }

        int getPriorityUp() {
            if (isPadding())
                return (0);
            if (isDummy()) {
                if (pred.get(0).isDummy())
                    return (Integer.MAX_VALUE); // part of a straight line
                else
                    return (Integer.MAX_VALUE >> 1); // start of a straight line
            }
            return (succ.size());
        }
    }

    private final List<ArrayList<NodeWrapper>> layers = new ArrayList<>(
            MAX_LAYERS);
    private final Map<NodeLayout, NodeWrapper> map = new IdentityHashMap<>();
    private final int direction;
    private final Dimension dimension;
    private LayoutContext context;

    private int last; // index of the last element in a layer after padding
    // process

    /**
     * Constructs a tree-like, layered layout of a directed graph.
     *
     * @param dir - {@link SugiyamaLayoutAlgorithm#HORIZONTAL}: left to right -
     *            {@link SugiyamaLayoutAlgorithm#VERTICAL}: top to bottom
     * @param dim - desired size of the layout area.
     */
    public SugiyamaLayoutAlgorithm(int dir, Dimension dim) {
        if (dir == HORIZONTAL)
            direction = HORIZONTAL;
        else
            direction = VERTICAL;
        dimension = dim;
    }

    public void setLayoutContext(LayoutContext context) {
        this.context = context;
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

        //removePadding();

        getXByBaryCentre();
//        for (int i =0; i < 1000; i++) {
//            relax();
//        }

        for (int i = 0; i < layers.size(); i++) { // reduce and refine
            for (NodeWrapper nw : layers.get(i)) {
                nw.x = nw.index;
            }
        }

        calculatePositions();
    }

    private void removePadding() {

        for (int i = 0; i < layers.size(); i++) {
            List<NodeWrapper> toRemove = new ArrayList<>();
            for (NodeWrapper n : layers.get(0)) {
                if (n.isPadding()) toRemove.add(n);
            }
            layers.removeAll(toRemove);
        }
    }

    public double SPRING_STRENGTH = 0.1;
    public double TORQUE_STRENGTH = 0.25;
    public static double TARGET_LEN = Math.sqrt(2.0);
    public double REPULSION_STRENGTH = 10;
    public double REPULSION_LIMIT = 2000.0;
    int REPULSION_TYPE = 0; // 0: (1-r)/r   1: 1-r   2: (1-r)^2
    public double FRICTION_FACTOR=0.75;

    public synchronized void relax() {

//        // go through all the nodes
//        for (int i = 0; i < layers.size(); i++) {
//            for (NodeWrapper n : layers.get(i)) {
//                n.dx = 0.0;
//            }
//        }

        // go through all the edges
        for (int i = 1; i < layers.size(); i++) {
            for (NodeWrapper to : layers.get(i)) {
                for (NodeWrapper from : to.pred) {
                    double vx = to.x - from.x;
                    double vy = to.layer - from.layer;
                    double len = Math.sqrt(vx * vx + vy * vy);

                    if (len > 0) {
                        double f = SPRING_STRENGTH * (TARGET_LEN - len) / len;
                        double dx = f * vx;
                        double dy = f * vy;

                        double phi = Math.atan2(vx, vy);
                        double dir = -Math.sin(4 * phi);
                        dx += TORQUE_STRENGTH * vy * dir / len;

                        to.dx += dx;
                        from.dx += -dx;
                    }
                }
            }
        }

        // go through all the nodes
        for (int i = 0; i < layers.size(); i++) {
            for (NodeWrapper n1 : layers.get(i)) {

                double dx = 0;
                double dy = 0;
                for (int j = 0; j < layers.size(); j++) {
                    for (NodeWrapper n2 : layers.get(j)) {

                        if (n1 == n2) {
                            continue;
                        }
                        double vx = n1.x - n2.x;
                        double vy = n1.layer - n2.layer;
                        double lensqr = vx * vx + vy * vy;
                        double len = Math.sqrt(lensqr);
                        if (len == 0) {
                            dx += REPULSION_STRENGTH * Math.random();
                            dy += REPULSION_STRENGTH * Math.random();
                        } else if (len < REPULSION_LIMIT) {
                            // Normalize length.
                            vx = vx / REPULSION_LIMIT;
                            vy = vy / REPULSION_LIMIT;
                            len = len / REPULSION_LIMIT;
                            // Compute force.
                            double f = 0;

                            switch (REPULSION_TYPE) {
                                case 0:
                                    f = 0.5 * (1 - len) / len;
                                    break;
                                case 1:
                                    f = 1 - len;
                                    break;
                                case 2:
                                    f = 2 * (1 - len) * (1 - len);
                                    break;
                            }

                            f *= REPULSION_STRENGTH;
                            dx += f * vx;
                            dy += f * vy;
                        }
                    }
                }
                n1.dx += dx;
            }
        }

        // go through all the nodes
        for (int i = 0; i < layers.size(); i++) {
            for (NodeWrapper n : layers.get(i)) {
                    n.x += Math.max(-5, Math.min(5, n.dx));
                    if (n.x < 0) {
                        n.x = 0;
                    }
                n.dx *= FRICTION_FACTOR;
            }
        }
    }

    private void getXByBaryCentre() {

        for (NodeWrapper nw : layers.get(0)) {
            nw.x = nw.index;
        }

        // remove padding
        for (int index = 1; index < layers.size(); index++)
            refineXLayersDown(layers.get(index));

    }

    private void refineXLayersDown(List<NodeWrapper> layer) {

        // second, remove padding from the layer's end and place them in front
        // of the current node to improve its position
        for (NodeWrapper iter : layer) {
            iter.x = iter.getXBaryCenter(iter.pred); // distance
        }
    }

    private void createLayers() {
        List<NodeLayout> nodes = new LinkedList<>();
        nodes.addAll(context.getNodes());
        List<NodeLayout> predecessors = findRoots(nodes);

        System.out.println("layer 0 = " + predecessors);

        nodes.removeAll(predecessors); // nodes now contains only nodes that are not roots;
        addLayer(predecessors);
        for (int level = 1; !nodes.isEmpty(); level++) {
            if (level > MAX_LAYERS)
                throw new RuntimeException(
                        "Graphical tree exceeds maximum depth of " + MAX_LAYERS
                                + "! (Graph not directed? Cycles?)");
            List<NodeLayout> layer = new ArrayList<>();
            for (NodeLayout item : nodes) {
                if (predecessors.containsAll(item.getPredecessingNodes()))
                    layer.add(item);
            }
            nodes.removeAll(layer);
            predecessors.addAll(layer);
            addLayer(layer);
            System.out.println("layer " + level + " = " + layer);
        }
    }

    private void createLayersFromSinks() {
        List<NodeLayout> nodes = new LinkedList<>();
        nodes.addAll(context.getNodes());
        List<NodeLayout> successors = findSinks(nodes);

        nodes.removeAll(successors); // nodes now contains only nodes that are not roots;

        List<List<NodeLayout>> layers = new ArrayList<>();

        List<NodeLayout> layer1 = new ArrayList<>();
        layer1.addAll(successors);
        layers.add(layer1);
        int level = 1;
        while (!nodes.isEmpty()) {
            if (level > MAX_LAYERS)
                throw new RuntimeException(
                        "Graphical tree exceeds maximum depth of " + MAX_LAYERS
                                + "! (Graph not directed? Cycles?)");
            List<NodeLayout> layer = new ArrayList<>();
            for (NodeLayout item : nodes) {
                if (successors.containsAll(item.getSuccessingNodes())) {
                    layer.add(item);
                }
            }
            nodes.removeAll(layer);
            successors.addAll(layer);
            layers.add(layer);
            level += 1;
        }
        for (int i = layers.size() - 1; i >= 0; i--) {
            System.out.println("layer " + (layers.size() - i - 1) + ":" + layers.get(i));
            addLayer(layers.get(i));
        }
    }

    /**
     * Wraps all {@link NodeLayout} objects into an internal presentation
     * {@link NodeWrapper} and inserts dummy wrappers into the layers between an
     * object and their predecessing nodes if necessary. Finally, all nodes are
     * chained over immediate adjacent layers down to their predecessors. This
     * is necessary to apply the final step of the Sugiyama algorithm to refine
     * the node position within a layer.
     *
     * @param list : List of all {@link NodeLayout} objects within the current
     *             layer
     */
    private void addLayer(List<NodeLayout> list) {
        ArrayList<NodeWrapper> layer = new ArrayList<>(list.size());
        for (NodeLayout node : list) {
            // wrap each NodeLayout with the internal data object and provide a
            // corresponding mapping
            NodeWrapper nw = new NodeWrapper(node, layers.size());
            map.put(node, nw);
            layer.add(nw);
            // insert dummy nodes if the adjacent layer does not contain the
            // predecessor
            for (NodeLayout node_predecessor : node.getPredecessingNodes()) { // for
                // all
                // predecessors
                NodeWrapper nw_predecessor = map.get(node_predecessor);
                for (int level = nw_predecessor.layer + 1; level < nw.layer; level++) {
                    // add "virtual" wrappers (dummies) to the layers in between
                    // virtual wrappers are in fact parts of a double linked
                    // list
                    NodeWrapper nw_dummy = new NodeWrapper(level);
                    nw_dummy.addPredecessor(nw_predecessor);
                    nw_predecessor.addSuccessor(nw_dummy);
                    nw_predecessor = nw_dummy;
                    layers.get(level).add(nw_dummy);
                }
                nw.addPredecessor(nw_predecessor);
                nw_predecessor.addSuccessor(nw);
            }
        }
        layers.add(layer);
        updateIndex(layer);
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

    private static void reduceCrossingsDown(ArrayList<NodeWrapper> layer) {
        // DOWN: scan PREDECESSORS
        for (NodeWrapper node : layer)
            node.index = node.getBaryCenter(node.pred);
        Collections.sort(layer, Comparator.comparingDouble(node -> node.index));
        updateIndex(layer);
    }

    private static void reduceCrossingsUp(ArrayList<NodeWrapper> layer) {
        // UP: scan SUCCESSORS
        for (NodeWrapper node : layer)
            node.index = node.getBaryCenter(node.succ);
        Collections.sort(layer, Comparator.comparingDouble(node -> node.index));
        updateIndex(layer);
    }

    /**
     * Fills in virtual nodes, so the layer system finally becomes an
     * equidistant grid
     */
    private void padLayers() {
        last = 0;
        for (List<NodeWrapper> iter : layers) {
            if (iter.size() > last) {
                last = iter.size();
            }
        }
        last--; // index of the last element of any layer
        for (List<NodeWrapper> iter : layers) { // padding is always added at
            // the END of each layer!
            for (int i = iter.size(); i <= last; i++)
                iter.add(new NodeWrapper());
            updateIndex(iter);
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

    private void refineLayersDown(List<NodeWrapper> layer) {
        // first, get a priority list
        List<NodeWrapper> list = new ArrayList<>(layer);
        Collections.sort(list, (node1, node2) -> {
            return (node2.getPriorityDown() - node1.getPriorityDown()); // descending
            // ordering!!!
        });
        // second, remove padding from the layer's end and place them in front
        // of the current node to improve its position
        for (NodeWrapper iter : list) {
            if (iter.isPadding())
                break; // break, if there are no more "real" nodes
            int delta = iter.getBaryCenter(iter.pred) - iter.index; // distance
            // to new
            // position
            for (int i = 0; i < delta; i++)
                layer.add(iter.index, layer.remove(last));
        }
        updateIndex(layer);
    }

    private void refineLayersUp(List<NodeWrapper> layer) {
        // first, get a priority list
        List<NodeWrapper> list = new ArrayList<>(layer);
        Collections.sort(list, (node1, node2) -> {
            return (node2.getPriorityUp() - node1.getPriorityUp()); // descending
            // ordering!!!
        });
        // second, remove padding from the layer's end and place them in front
        // of the current node to improve its position
        for (NodeWrapper iter : list) {
            if (iter.isPadding())
                break; // break, if there are no more "real" nodes
            int delta = iter.getBaryCenter(iter.succ) - iter.index; // distance
            // to new
            // position
            for (int i = 0; i < delta; i++)
                layer.add(iter.index, layer.remove(last));
        }
        updateIndex(layer);
    }

    private void calculatePositions() {

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        for (int i = 0; i < layers.size(); i++) {
            for (NodeWrapper n : layers.get(i)) {
                if (n.x > maxX) maxX = n.x;
                if (n.x < minX) minX = n.x;
            }
        }

        double dx = dimension.getWidth() / layers.size();
        double dy = dimension.getHeight() / ((maxX-minX) + 1);

        if (direction == VERTICAL) {
            dx = dimension.getWidth() / ((maxX-minX) + 1);
            dy = dimension.getHeight() / layers.size();
        }

        if (direction == HORIZONTAL)
            for (NodeLayout node : context.getNodes()) {
                NodeWrapper nw = map.get(node);
                node.setLocation((nw.layer + 0.5d) * dx, (nw.x-minX + 0.5d) * dy);
            }
        else
            for (NodeLayout node : context.getNodes()) {
                NodeWrapper nw = map.get(node);
                node.setLocation((nw.x-minX + 0.5d) * dx, (nw.layer + 0.5d) * dy);
            }
    }

    private static List<NodeLayout> findRoots(List<NodeLayout> list) {
        List<NodeLayout> roots = new ArrayList<>();
        for (NodeLayout iter : list) { // no predecessors means: this is a root,
            // add it to list
            if (iter.getPredecessingNodes().size() == 0)
                roots.add(iter);
        }
        return roots;
    }

    private static List<NodeLayout> findSinks(List<NodeLayout> list) {
        List<NodeLayout> sinks = new ArrayList<>();
        for (NodeLayout iter : list) { // no successors means: this is a sink,
            // add it to list
            if (iter.getSuccessingNodes().size() == 0) {
                sinks.add(iter);
            }
        }
        return sinks;
    }

    private static void updateIndex(List<NodeWrapper> list) {
        for (int index = 0; index < list.size(); index++)
            list.get(index).index = index;
    }
}
