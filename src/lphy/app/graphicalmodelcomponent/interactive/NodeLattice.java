package lphy.app.graphicalmodelcomponent.interactive;

import lphy.app.graphicalmodelcomponent.LayeredNode;
import lphy.app.graphicalmodelcomponent.Positioning;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.geom.Ellipse2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;

public class NodeLattice {

    Positioning positioning;
    JComponent component;
    Insets insets;

    public NodeLattice(Positioning positioning, JComponent component, Insets insets) {
        this.positioning = positioning;
        this.component = component;
        this.insets = insets;

        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
            }
        });
    }

    public void setToNearest(LayeredNode node) {
        LatticePoint position = positioning.getNearestPosition(node.getX(), node.getY());
        node.setMetaData(LatticePoint.KEY, position);

        Point2D point2D = positioning.getPoint2D(node);

        node.setPosition(point2D.getX(), point2D.getY());
    }


    public void positionAllNodes(List<LayeredNode> nodes) {
        for (LayeredNode node : nodes) {
            setToNearest(node);
        }
    }

    public void paint(Graphics2D g2d) {
        g2d.setColor(Color.lightGray);

        for (int i = positioning.getMinLatticeX(); i <= positioning.getMaxLatticeX(); i++) {
            for (int j = positioning.getMinLatticeY(); j <= positioning.getMaxLatticeY(); j++) {

                LatticePoint latticePoint = new LatticePoint(i,j);
                Point2D point2D = positioning.getPoint2D(latticePoint);

                Ellipse2D ellipse2D = new Ellipse2D.Double(point2D.getX()-2.0, point2D.getY()-2.0, 4,4);
                g2d.fill(ellipse2D);
                //g2d.drawString(latticePoint.toString(), (int)point2D.getX()+10, (int)point2D.getY());
            }
        }
        g2d.setColor(Color.black);
    }
}
