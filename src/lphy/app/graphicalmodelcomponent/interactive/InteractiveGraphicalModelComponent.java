package lphy.app.graphicalmodelcomponent.interactive;

import lphy.app.GraphicalLPhyParser;
import lphy.app.GraphicalModelChangeListener;
import lphy.app.GraphicalModelListener;
import lphy.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphy.app.graphicalmodelcomponent.LayeredGraph;
import lphy.app.graphicalmodelcomponent.LayeredNode;
import lphy.core.LPhyParser;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

/**
 * Created by adru001 on 18/12/19.
 */
public class InteractiveGraphicalModelComponent extends JComponent {

    public static Preferences preferences = Preferences.userNodeForPackage(InteractiveGraphicalModelComponent.class);

    LayeredGraph layeredGraph;

    NodeLattice lattice;

    int BORDER = 20;
    Insets insets = new Insets(BORDER, BORDER, BORDER, BORDER);

    int nodeSize = 20;

    LayeredNode selectedNode = null;

    GraphicalModelComponent component = null;

    public InteractiveGraphicalModelComponent(GraphicalLPhyParser parser, GraphicalModelComponent component) {

        this.component = component;
        insets = component.insets;

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectedNode = lattice.getNode(lattice.getNearestPosition(e.getPoint()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedNode != null) {
                    lattice.reposition(selectedNode);
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

    public void setLayeredGraph(LayeredGraph layeredGraph) {
        this.layeredGraph = layeredGraph;
        lattice = new NodeLattice((layeredGraph.getMaxIndex()+1)*4, layeredGraph.getLayerCount()*4, this, insets);

        layeredGraph.getNodes().stream().forEach(n -> lattice.addNode(n));

    }

    public void paintComponent(Graphics g) {

        if (layeredGraph == null) {
            setLayeredGraph(component.getLayeredGraph());
        }

        lattice.paint((Graphics2D)g);

        for (LayeredNode node : layeredGraph.getNodes()) {
            paintSuccessorEdges(node, (Graphics2D) g);
        }


        for (LayeredNode node : layeredGraph.getNodes()) {
            paintNode(node, (Graphics2D) g);
        }
    }

    private void paintNode(LayeredNode node, Graphics2D g2d) {

        Rectangle2D rectangle2D = new Rectangle2D.Double(node.getX() - nodeSize / 2, node.getY() - nodeSize / 2, nodeSize, nodeSize);

        g2d.draw(rectangle2D);


    }

    private void paintSuccessorEdges(LayeredNode node, Graphics2D g2d) {

        for (LayeredNode successor : node.getSuccessors()) {
            Line2D line2D = new Line2D.Double(node.getPosition(), successor.getPosition());
            g2d.draw(line2D);
        }
    }
}