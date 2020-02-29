package james.app.graphicalmodelcomponent;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

class NodeWrapper implements LayeredNode {
    private int index; // index within layer
    private int layer;
    double dx = 0.0;
    private double x;
    private double y;
    private final LayeredNode node;
    private final List<LayeredNode> pred = new LinkedList<>();
    private final List<LayeredNode> succ = new LinkedList<>();
    boolean padding = false;

    NodeWrapper(LayeredNode n, int layer) {
        node = n;
        this.layer = layer;
        if (n != null && n.getLayer() != layer) {
            throw new IllegalArgumentException();
        }
    } // NodeLayout wrapper

    NodeWrapper(int l) {
        this(null, l);
    } // Dummy to connect two NodeLayout objects

    NodeWrapper() {
        this(null,Integer.MAX_VALUE);
        padding = true;
    } // Padding for final refinement phase

    void addPredecessor(NodeWrapper node) {
        pred.add(node);
    }

    void addSuccessor(NodeWrapper node) {
        succ.add(node);
    }

    boolean isDummy() {
        return ((node == null) && !padding);
    }

    boolean isPadding() {
        return ((node == null) && padding);
    }

    int getBaryCenter(List<LayeredNode> list) {
        if (list.isEmpty())
            return (this.index);
        double barycenter = 0.0;
        for (LayeredNode node : list)
            barycenter += node.getIndex();
        return (int) Math.round(barycenter / list.size()); // always rounding off to
        // avoid wrap around in
        // position refining!!!
    }

    public double getXBaryCenter(List<LayeredNode> list) {
        if (list.isEmpty())
            return (this.x);
        double barycenter = 0.0;
        for (LayeredNode node : list)
            barycenter += node.getX();
        return barycenter / (double)list.size();
    }


    int getPriorityDown() {
        if (isPadding())
            return (0);
        if (isDummy()) {
            if (((NodeWrapper)succ.get(0)).isDummy())
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
            if (((NodeWrapper)pred.get(0)).isDummy())
                return (Integer.MAX_VALUE); // part of a straight line
            else
                return (Integer.MAX_VALUE >> 1); // start of a straight line
        }
        return (succ.size());
    }

    @Override
    public List<LayeredNode> getSuccessors() {
        return succ;
    }

    @Override
    public List<LayeredNode> getPredecessors() {
        return pred;
    }

    @Override
    public int getLayer() {
        if (node != null) return node.getLayer();
        else return layer;
    }

    @Override
    public void setLayer(int layer) {
        if (node != null) node.setLayer(layer);
        this.layer = layer;
    }

    @Override
    public double getX() {
        if (node != null) return node.getX();
        else return x;
    }

    public double getY() {
        if (node != null) return node.getY();
        else return y;
    }

    public Point2D getPosition() {
        return new Point2D.Double(getX(), getY());
    }

    public void setX(double x) {
        if (node != null) node.setX(x);
        this.x = x;
    }

    public void setY(double y) {
        if (node != null) node.setY(y);
        this.y = y;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    public String toString() {
        if (isPadding()) return "padding";
        if (isDummy()) return "dummy";
        return "wrapper(" + node.toString() + ")";
    }
}
