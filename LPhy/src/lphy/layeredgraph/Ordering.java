package lphy.layeredgraph;

import java.util.Comparator;
import java.util.List;

public class Ordering {

    public static int MAX_ITERATIONS = 8;

    public enum DIRECTION {
        up, down
    }

    public void order(ProperLayeredGraph layeredGraph) {

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            wmedian(layeredGraph, i % 2 == 0 ? DIRECTION.down : DIRECTION.up);
        }

    }

    private void wmedian(ProperLayeredGraph layeredGraph, DIRECTION direction) {
        if (direction == DIRECTION.down) {
            for (int r = 1; r < layeredGraph.getLayerCount(); r++) {
                List<LayeredNode> layer = layeredGraph.getLayer(r);
                for (LayeredNode node : layer) {
                    node.setX(medianValue(node, node.getPredecessors()));
                }
                layer.sort(Comparator.comparingDouble(LayeredNode::getX));
                layeredGraph.updateIndex(r);
            }
        } else {
            for (int r = layeredGraph.getLayerCount()-2; r >=0; r--) {
                List<LayeredNode> layer = layeredGraph.getLayer(r);
                for (LayeredNode node : layer) {
                    node.setX(medianValue(node, node.getSuccessors()));
                }
                layer.sort(Comparator.comparingDouble(LayeredNode::getX));
                layeredGraph.updateIndex(r);
            }
        }
    }

    private double medianValue(LayeredNode v, List<LayeredNode> adjacentNodes) {
        int size = adjacentNodes.size();

        if (size == 0) return v.getIndex();

        int m = adjacentNodes.size() / 2;
        if (size % 2 == 1) {
            return adjacentNodes.get(m).getIndex();
        } else if (size == 2) {
            return 0.5;
        } else {
            double left = m-1;
            double right = size - m - 1;
            return (m-1)*right + m*left / (left+right);
        }
    }
}
