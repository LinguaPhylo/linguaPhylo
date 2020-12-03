package lphy.app.graphicalmodelcomponent.interactive;

import lphy.app.graphicalmodelcomponent.LayeredNode;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;

public class NodeLattice {

    int latticeWidth;
    int latticeHeight;
    JComponent component;
    Insets insets;

    double xStep;
    double yStep;

    List<LayeredNode>[][] nodes;

    public NodeLattice(int latticeWidth, int latticeHeight, JComponent component, Insets insets) {
        this.latticeWidth = latticeWidth;
        this.latticeHeight = latticeHeight;

        if (latticeWidth < 1 || latticeHeight < 1) throw new RuntimeException();

        nodes = new List[this.latticeWidth][this.latticeHeight];
        for (int i = 0; i < latticeWidth; i++) {
            for (int j = 0; j < latticeHeight; j++) {
                nodes[i][j] = new ArrayList<>();
            }
        }

        this.component = component;
        this.insets = insets;
        recalculate();

        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                recalculate();
            }
        });
    }

    private void recalculate() {
        xStep = (component.getWidth() - insets.left - insets.right) / (latticeWidth - 1.0);
        yStep = (component.getHeight() - insets.top - insets.bottom) / (latticeHeight - 1.0);
        positionAllNodes();
    }

    public Point2D getPoint(Position pos) {
        double x = pos.x * xStep + insets.left;
        double y = component.getHeight() - insets.bottom - (latticeHeight - 1 - pos.y) * yStep;

        return new Point2D.Double(x, y);
    }

    public Position getNearestPosition(Point2D point2D) {
        int x = (int) Math.round((point2D.getX() - insets.left) / xStep);
        if (x < 0) x = 0;
        if (x > latticeWidth - 1) x = latticeWidth - 1;

        int y = (latticeHeight - 1) - (int) Math.round(((component.getHeight() - insets.bottom) - point2D.getY()) / yStep);
        if (y < 0) y = 0;
        if (y > latticeHeight - 1) y = latticeHeight - 1;

        return new Position(x, y);
    }

    public Position getNearestPosition(Point point) {
        int x = (int) Math.round((point.getX() - insets.left) / xStep);
        if (x < 0) x = 0;
        if (x > latticeWidth - 1) x = latticeWidth - 1;

        int y = (latticeHeight - 1) - (int) Math.round(((component.getHeight() - insets.bottom) - point.getY()) / yStep);
        if (y < 0) y = 0;
        if (y > latticeHeight - 1) y = latticeHeight - 1;

        return new Position(x, y);
    }

    public void addNode(LayeredNode node) {
        Position position = getNearestPosition(new Point2D.Double(node.getX(), node.getY()));
        nodes[position.x][position.y].add(node);
        Point2D point2D = getPoint(position);
        node.setPosition(point2D.getX(), point2D.getY());
    }

    public LayeredNode getNode(Position position) {
        if (position.x < 0 || position.y < 0 || position.x > (latticeWidth - 1) || position.y > (latticeHeight - 1)) {
            throw new RuntimeException("Invalid position!" + position);
        }

        List<LayeredNode> nodeList = nodes[position.x][position.y];
        if (nodeList.size() > 0) return nodeList.get(nodeList.size() - 1);
        return null;
    }

    public void reposition(LayeredNode selectedNode) {
        remove(selectedNode);
        addNode(selectedNode);
    }

    public void remove(LayeredNode layeredNode) {

        // TODO super inefficient!
        for (int i = 0; i < latticeWidth; i++) {
            for (int j = 0; j < latticeHeight; j++) {
                if (nodes[i][j].contains(layeredNode)) nodes[i][j].remove(layeredNode);
            }
        }
    }

    public void positionAllNodes() {
        for (int i = 0; i < latticeWidth; i++) {
            for (int j = 0; j < latticeHeight; j++) {
                for (LayeredNode node : nodes[i][j]) {
                    Point2D point2D = getPoint(new Position(i, j));
                    node.setPosition(point2D.getX(), point2D.getY());
                }
            }
        }
    }

    public void paint(Graphics2D g2d) {
        g2d.setColor(Color.lightGray);

        for (int i = 0; i < latticeWidth; i++) {
            for (int j = 0; j < latticeHeight; j++) {
                Point2D point2D = getPoint(new Position(i,j));

                Ellipse2D ellipse2D = new Ellipse2D.Double(point2D.getX()-2.0, point2D.getY()-2.0, 4,4);
                g2d.fill(ellipse2D);
            }
        }
        g2d.setColor(Color.black);
    }
}
