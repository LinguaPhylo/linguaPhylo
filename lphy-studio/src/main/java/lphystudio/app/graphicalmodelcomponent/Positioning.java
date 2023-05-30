package lphystudio.app.graphicalmodelcomponent;

import lphy.core.layeredgraph.LatticePoint;
import lphy.core.layeredgraph.LayeredGraph;
import lphy.core.layeredgraph.LayeredNode;
import lphy.core.layeredgraph.ProperLayeredGraph;

import java.awt.*;
import java.awt.geom.Point2D;

public class Positioning {

    private double dx, dy;
    private int minLatticeX, maxLatticeX;
    private int minLatticeY, maxLatticeY;
    private Insets insets;
    private Dimension dimension;

    ProperLayeredGraph properLayeredGraph;

    public void position(ProperLayeredGraph layeredGraph, Dimension dimension, Insets insets) {

        getMinMaxLatticeXY(layeredGraph);
        this.dimension = dimension;
        properLayeredGraph = layeredGraph;

        dx = (dimension.getWidth() - insets.left - insets.right) / ((maxLatticeX - minLatticeX)+1);

        dy = (dimension.getHeight() - insets.top - insets.bottom) / ((maxLatticeY - minLatticeY)+1);

        this.insets = insets;

        for (LayeredNode node : layeredGraph.getNodes()) {
            Point2D point2D = getPoint2D(node);
            node.setPosition(point2D.getX(), point2D.getY());
        }
    }

    public int getMinLatticeX() {
        return minLatticeX;
    }

    public int getMinLatticeY() {
        return minLatticeY;
    }

    public int getMaxLatticeX() {
        return maxLatticeX;
    }

    public int getMaxLatticeY() {
        return maxLatticeY;
    }

    public void getMinMaxLatticeXY(LayeredGraph layeredGraph) {
        maxLatticeX = Integer.MIN_VALUE;
        minLatticeX = Integer.MAX_VALUE;
        maxLatticeY = Integer.MIN_VALUE;
        minLatticeY = Integer.MAX_VALUE;

        for (LayeredNode node : layeredGraph.getNodes()) {
            LatticePoint latticePoint = (LatticePoint)node.getMetaData(LatticePoint.KEY);
            if (latticePoint != null) {
                if (latticePoint.x > maxLatticeX) maxLatticeX = latticePoint.x;
                if (latticePoint.x < minLatticeX) minLatticeX = latticePoint.x;
                if (latticePoint.y > maxLatticeY) maxLatticeY = latticePoint.y;
                if (latticePoint.y < minLatticeY) minLatticeY = latticePoint.y;
            }
        }
    }

    public Point2D getPoint2D(LayeredNode node) {

        LatticePoint latticePoint = (LatticePoint)node.getMetaData(LatticePoint.KEY);

        if (latticePoint != null) return getPoint2D(latticePoint);

        else return getPoint2D(new LatticePoint((int)node.getX(), node.getLayer()));
    }

    public Point2D getPoint2D(LatticePoint latticePoint) {

        return new Point2D.Double((latticePoint.x-minLatticeX+0.5) * dx + insets.left, (latticePoint.y-minLatticeY+0.5) * dy + insets.top);
    }

    public LatticePoint getNearestPosition(double px, double py) {
        int col = (int) Math.round((px - insets.left) / dx - 0.5);
        if (col < minLatticeX) col = minLatticeX;
        if (col > maxLatticeX) col = maxLatticeX;

        int y = (maxLatticeY-minLatticeY) - (int) Math.round(((dimension.getHeight() - insets.bottom) - py) / dy - 0.5);
        if (y < minLatticeY) y = minLatticeY;
        if (y >= maxLatticeY) y = maxLatticeY;

        return new LatticePoint(col, y);
    }

    public LayeredNode getNode(LatticePoint position) {
        for (LayeredNode node : properLayeredGraph.getNodes()) {
            if (position.equals(node.getMetaData(LatticePoint.KEY))) return node;
        }
        return null;
    }

    public LatticePoint getNearestPosition(Point point) {
        return getNearestPosition(point.x, point.y);
    }
}
