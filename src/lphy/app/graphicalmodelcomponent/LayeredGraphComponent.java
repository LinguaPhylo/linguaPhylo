package lphy.app.graphicalmodelcomponent;

import lphy.layeredgraph.LayeredGraph;
import lphy.layeredgraph.LayeredNode;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class LayeredGraphComponent extends JComponent {

    LayeredGraph graph;

    int VERTEX_SIZE = 10;

    public LayeredGraphComponent(LayeredGraph graph) {
        this.graph = graph;
    }

    @Override
    public void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D)g;

        int layerCount = graph.getLayerCount();

        double maxX = graph.getMaxX();

        double dy = getHeight() / graph.getLayerCount();

        double dx = getWidth() / (maxX+1);

        double y = dy / 2.0;
        for (int i = 0; i < layerCount; i++) {
            Line2D line = new Line2D.Double(0.0, y, getWidth(), y);
            g2d.draw(line);

            for (LayeredNode v : graph.getLayer(i)) {
                double x = (v.getX() + 0.5) * dx;

                if (i > 0) {
                    for (LayeredNode u : v.getPredecessors()) {
                        double ux = (u.getX() + 0.5) * dx;
                        double uy = (u.getLayer() + 0.5) * dy;
                        Line2D edge = new Line2D.Double(ux, uy, x, y);
                        g2d.draw(edge);
                    }
                }
            }
            y += dy;
        }

        y = dy / 2.0;
        for (int i = 0; i < layerCount; i++) {

            for (LayeredNode v : graph.getLayer(i)) {
                double x = (v.getX() + 0.5) * dx;

                Ellipse2D ellipse2D = new Ellipse2D.Double(x-VERTEX_SIZE/2, y-VERTEX_SIZE/2, VERTEX_SIZE, VERTEX_SIZE);

                if (v.isDummy()) {
                    g2d.setColor(g2d.getBackground());
                    g2d.fill(ellipse2D);
                    g2d.setColor(Color.black);
                    g2d.draw(ellipse2D);
                } else {
                    g2d.fill(ellipse2D);
                }
            }
            y += dy;
        }
    }
}
