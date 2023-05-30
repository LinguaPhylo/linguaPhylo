package lphy.core.layeredgraph;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NodeWrapper implements LayeredNode {
    private int index; // index within layer
    private int layer;
    double dx = 0.0;
    private double x;
    private double y;
    private final LayeredNode node;
    private final List<LayeredNode> pred = new LinkedList<>();
    private final List<LayeredNode> succ = new LinkedList<>();
    boolean padding = false;
    Map<String, Object> metaData = new TreeMap<>();

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

    public boolean isDummy() {
        return ((node == null) && !padding);
    }

    boolean isPadding() {
        return ((node == null) && padding);
    }

    public LayeredNode wrappedNode() {
        return node;
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
    public Object getMetaData(String key) {
        if (node != null) return node.getMetaData(key);
        return metaData.get(key);
    }

    @Override
    public void setMetaData(String name, Object value) {
        if (node != null) {
            node.setMetaData(name, value);
        } else {
            metaData.put(name, value);
        }
    }

    @Override
    public void setLayer(int layer) {
        if (node != null) {
            node.setLayer(layer);
        } else this.layer = layer;
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
