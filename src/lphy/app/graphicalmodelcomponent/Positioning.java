package lphy.app.graphicalmodelcomponent;

import lphy.app.graphicalmodelcomponent.interactive.Position;

import java.awt.*;
import java.awt.geom.Point2D;

public class Positioning {

    private double dx, dy;
    private int minColumn, maxColumn;
    private int layerCount;
    private Insets insets;
    private Dimension dimension;

    ProperLayeredGraph properLayeredGraph;

    public void position(ProperLayeredGraph layeredGraph, Dimension dimension, Insets insets) {
        minColumn = layeredGraph.getMinColumn();
        maxColumn = layeredGraph.getMaxColumn();
        this.dimension = dimension;
        this.layerCount = layeredGraph.layers.size();
        properLayeredGraph = layeredGraph;

        dx = (dimension.getWidth() - insets.left - insets.right) / ((maxColumn-minColumn)+1);

        dy = (dimension.getHeight() - insets.top - insets.bottom) / layerCount;

        this.insets = insets;

        for (LayeredNode node : layeredGraph.getNodes()) {
            Point2D point2D = getPosition(node);
            node.setPosition(point2D.getX(), point2D.getY());
        }
    }

    public int getMaxColumn() {
        return maxColumn;
    }

    public int getMinColumn() {
        return minColumn;
    }

    public Point2D getPosition(LayeredNode node) {
        return getPosition(node.getColumn(), node.getLayer());
    }

    public Point2D getPosition(int col, int layer) {
        return new Point2D.Double((col+0.5) * dx + insets.left, (layer+0.5) * dy + insets.top);
    }

    public Position getNearestPosition(double px, double py) {
        int col = (int) Math.round((px - insets.left) / dx - 0.5);
        if (col < minColumn) col = minColumn;
        if (col > maxColumn) col = maxColumn;

        int y = (layerCount - 1) - (int) Math.round(((dimension.getHeight() - insets.bottom) - py) / dy - 0.5);
        if (y < 0) y = 0;
        if (y >= layerCount) y = layerCount - 1;

        return new Position(col, y);
    }

    public LayeredNode getNode(Position position) {
        for (LayeredNode node : properLayeredGraph.getNodes()) {
            if (node.getColumn() == position.x && node.getLayer() == position.y) return node;
        }
        return null;
    }

    public Position getNearestPosition(Point point) {
        return getNearestPosition(point.x, point.y);
    }

    public int getMaxLayer() {
        return layerCount-1;
    }
}
