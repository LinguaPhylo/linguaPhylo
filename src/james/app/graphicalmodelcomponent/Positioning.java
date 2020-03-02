package james.app.graphicalmodelcomponent;

import java.awt.*;

public class Positioning {

    public void position(ProperLayeredGraph layeredGraph, Dimension dimension, Insets insets) {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        for (int i = 0; i < layeredGraph.layers.size(); i++) {
            for (LayeredNode n : layeredGraph.layers.get(i)) {
                if (n.getX() > maxX) maxX = n.getX();
                if (n.getX() < minX) minX = n.getX();
            }
        }

        double dx = (dimension.getWidth() - insets.left - insets.right) / (maxX - minX);
        double dy = (dimension.getHeight() - insets.top - insets.bottom) / (layeredGraph.layers.size() - 1);

        for (LayeredNode node : layeredGraph.getNodes()) {
            node.setPosition((node.getX() - minX) * dx + insets.left, node.getLayer() * dy + insets.top);
        }
    }
}
