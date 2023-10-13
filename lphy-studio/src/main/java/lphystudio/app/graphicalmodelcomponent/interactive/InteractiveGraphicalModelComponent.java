package lphystudio.app.graphicalmodelcomponent.interactive;

import lphy.core.parser.LPhyMetaData;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelpanel.GraphicalModelContainer;
import lphystudio.core.layeredgraph.LayeredNode;
import lphystudio.core.layeredgraph.NodePaintUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.prefs.Preferences;

/**
 * Created by adru001 on 18/12/19.
 */
public class InteractiveGraphicalModelComponent extends JComponent {

    public static Preferences preferences = Preferences.userNodeForPackage(InteractiveGraphicalModelComponent.class);


    LPhyMetaData parser;
    NodeLattice lattice;

    int nodeSize = 20;

    LayeredNode selectedNode = null;

    GraphicalModelComponent component = null;

    public InteractiveGraphicalModelComponent(GraphicalModelContainer parser, GraphicalModelComponent component) {

        this.parser = parser;
        this.component = component;
        lattice = new NodeLattice(component.positioning, this, component.insets);


        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectedNode = component.positioning.getNode(component.positioning.getNearestPosition(e.getPoint()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedNode != null) {
                    lattice.setToNearest(selectedNode);
                }
                selectedNode = null;
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedNode != null) {
                    selectedNode.setX(e.getX());
                    selectedNode.setY(e.getY());
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });

    }

    public void paintComponent(Graphics g) {

        lattice.paint((Graphics2D)g);

        for (LayeredNode node : component.properLayeredGraph.getNodes()) {
            NodePaintUtils.paintNodeEdges(node,(Graphics2D)g, false, GraphicalModelComponent.getUseStraightEdges());
        }

        for (LayeredNode node : component.properLayeredGraph.getNodes()) {
            NodePaintUtils.paintNode(node,(Graphics2D)g, this, parser);
        }
    }

}