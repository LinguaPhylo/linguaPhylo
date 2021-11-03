package lphy.layeredgraph;

import java.awt.geom.Point2D;
import java.util.*;

public interface LayeredNode {

    /**
     * @return the nodes who connected to this node by outgoing edges.
     * By definition of a layered node they will have a layer greater than that of this node.
     */
    List<LayeredNode> getSuccessors();

    /**
     * @return the nodes who connected to this node by incoming edges.
     * By definition of a layered node they will have a layer index smaller than that of this node.
     */
    List<LayeredNode> getPredecessors();

    default boolean isSink() {
        return getSuccessors().size() == 0;
    }

    default boolean isSource() {
        return getPredecessors().size() == 0;
    }

    /**
     * @return the layer of this node.
     */
    int getLayer();

    /**
     * @return the column that this node is in
     */
    //int getColumn();

    /**
     * @param key the key of the metadata to retrieve
     * @return the metadata object
     */
    Object getMetaData(String key);

    /**
     * @param name the key of the metadata to set
     * @param value the value of the metadata to set
     */
    void setMetaData(String name, Object value);

    /**
     * @param layer the new layer of this node
     */
    void setLayer(int layer);
    

    default void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    boolean isDummy();

    default Point2D getPosition() {
        return new Point2D.Double(getX(), getY());
    }

    double getX();

    double getY();

    void setX(double x);

    void setY(double y);

    /**
     * @return the index of node within its layer
     */
    int getIndex();

    /**
     * @param index the index of the node in its layer
     */
    void setIndex(int index);

    class Default implements LayeredNode {

        protected double x = 0.0;
        protected double y = 0.0;
        private List<LayeredNode> successors = new ArrayList<>();
        private List<LayeredNode> predecessors = new ArrayList<>();
        protected int layer;
        int index;

        Map<String, Object> metadata = new TreeMap<>();

        public Default(int layer, int index) {
            this.layer = layer;
            this.index = index;
        }

        @Override
        public List<LayeredNode> getSuccessors() {
            return successors;
        }

        @Override
        public List<LayeredNode> getPredecessors() {
            return predecessors;
        }

        public void addSuccessor(LayeredNode successor) {
            getSuccessors().add(successor);
            if (!successor.getPredecessors().contains(this)) {
                successor.getPredecessors().add(this);
            };
        }

        @Override
        public int getLayer() {
            return layer;
        }

        @Override
        public Object getMetaData(String key) {
            return metadata.get(key);
        }

        @Override
        public void setMetaData(String name, Object value) {
            metadata.put(name, value);
        }

        @Override
        public void setLayer(int layer) {
            this.layer = layer;
        }

        @Override
        public boolean isDummy() {
            return false;
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public void setX(double x) {
            this.x = x;
        }

        @Override
        public void setY(double y) {
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

        public boolean isSource() {
            return predecessors.size() == 0;
        }

        public String toString() {
            return "v(" + layer + "," + index + ")";
        }
    }

    class Dummy extends Default {

        public Dummy(int layer) {
            super(layer, 0);
        }

        public Dummy(int layer, int index) {
            super(layer, index);
        }


        public boolean isDummy() {
            return true;
        }

        public String toString() {
            return "dummy(" + layer + "," + index + ")";
        }
    }
}
