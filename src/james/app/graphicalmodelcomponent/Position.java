package james.app.graphicalmodelcomponent;

import java.awt.*;

public class Position {

    public void position(ProperLayeredGraph layeredGraph, Dimension dimension) {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        for (int i = 0; i < layeredGraph.layers.size(); i++) {
            for (LayeredNode n : layeredGraph.layers.get(i)) {
                if (n.getX() > maxX) maxX = n.getX();
                if (n.getX() < minX) minX = n.getX();
            }
        }

        double dx = dimension.getWidth() / ((maxX - minX) + 1);
        double dy = dimension.getHeight() / layeredGraph.layers.size();

        for (LayeredNode node : layeredGraph.getNodes()) {
            node.setPosition((node.getX() - minX + 0.5d) * dx, (node.getLayer() + 0.5d) * dy);
        }
    }
}
