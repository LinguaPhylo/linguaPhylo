package james;

import java.awt.*;
import java.util.Arrays;

/**
 * @author Alexei Drummond
 */
public class NodeDecorator  {

    public double getNodeSize() {
        return 1.0;
    }

    public NodeShape getNodeShape() {
        return NodeShape.circle;
    }

    public Color getNodeColor() {
        return Color.black;
    }

    public boolean drawNodeShape() {
        return false;
    }

    enum NodeShape {circle, square, triangle}

    public static final NodeDecorator BLACK_DOT = new NodeDecorator() {
        public Color getNodeColor() {
            return Color.black;
        }

        public double getNodeSize() {
            return 4;
        }

        public NodeShape getNodeShape() {
            return NodeShape.circle;
        }

        public boolean drawNodeShape() {
            return false;
        }
    };

    public NodeDecorator() {
    }
}
