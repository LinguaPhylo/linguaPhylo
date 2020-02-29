package james.app.graphicalmodelcomponent;

import james.TimeTree;
import james.graphicalModel.*;
import james.graphicalModel.types.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
     * @param layer the new layer of this node
     */
    void setLayer(int layer);

    default void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    default double getXBarycenter(boolean predecessors, boolean successors) {
        double centre = 0.0;
        int count = 0;
        if (predecessors) {
            for (LayeredNode pred : getPredecessors()) {
                centre += pred.getX();
            }
            count += getPredecessors().size();
        }
        if (successors) {
            for (LayeredNode pred : getSuccessors()) {
                centre += pred.getX();
            }
            count += getSuccessors().size();
        }
        if (count == 0) return getX();

        return centre / (double)count;
    }

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
}
