package james;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * @author Alexei Drummond
 */
public interface TreeDrawingOrientation {

    AffineTransform getTransform(Rectangle2D bounds);

    Object getLeafLabelAnchor();

    Object getNodeHeightLabelAnchor();

    Object getBranchLabelAnchor();

    TreeDrawingOrientation UP = new TreeDrawingOrientation() {

        @Override
        public AffineTransform getTransform(Rectangle2D bounds) {
            //canonical coordinates are node positions:left->right heights:top->bottom
            // for horizontal left-to-right we want:
            // node positions: top->bottom
            // heights: right->left

            AffineTransform scale = AffineTransform.getScaleInstance(bounds.getWidth(), bounds.getHeight());
            AffineTransform finalTranslate = AffineTransform.getTranslateInstance(bounds.getMinX(), bounds.getMinY());

            finalTranslate.concatenate(scale);
            return finalTranslate;
        }

        @Override
        public Object getLeafLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_SOUTH;
        }

        @Override
        public Object getNodeHeightLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_WEST;
        }


        @Override
        public Object getBranchLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_EAST;
        }
    };

    TreeDrawingOrientation RIGHT = new TreeDrawingOrientation() {

        @Override
        public AffineTransform getTransform(Rectangle2D bounds) {
            //canonical coordinates are node positions:left->right heights:top->bottom
            // for horizontal left-to-right we want:
            // node positions: top->bottom
            // heights: right->left

            AffineTransform translate = AffineTransform.getTranslateInstance(-0.5, -0.5);
            AffineTransform rotation = AffineTransform.getRotateInstance(Math.PI / 2.0);
            AffineTransform scale = AffineTransform.getScaleInstance(bounds.getWidth(), bounds.getHeight());
            AffineTransform finalTranslate = AffineTransform.getTranslateInstance(bounds.getMinX() + bounds.getWidth() / 2.0, bounds.getMinY() + bounds.getHeight() / 2.0);

            finalTranslate.concatenate(scale);
            finalTranslate.concatenate(rotation);
            finalTranslate.concatenate(translate);
            return finalTranslate;
        }

        @Override
        public Object getLeafLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_WEST;
        }

        @Override
        public Object getNodeHeightLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_NORTH;
        }

        @Override
        public Object getBranchLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_SOUTH;
        }
    };

    TreeDrawingOrientation LEFT = new TreeDrawingOrientation() {
        @Override
        public AffineTransform getTransform(Rectangle2D bounds) {

            AffineTransform translate = AffineTransform.getTranslateInstance(-0.5, -0.5);
            AffineTransform rotation = AffineTransform.getRotateInstance(-Math.PI / 2.0);
            AffineTransform flipY = AffineTransform.getScaleInstance(1, -1);
            AffineTransform scale = AffineTransform.getScaleInstance(bounds.getWidth(), bounds.getHeight());
            AffineTransform finalTranslate = AffineTransform.getTranslateInstance(bounds.getMinX() + bounds.getWidth() / 2.0, bounds.getMinY() + bounds.getHeight() / 2.0);

            finalTranslate.concatenate(scale);
            finalTranslate.concatenate(flipY);
            finalTranslate.concatenate(rotation);
            finalTranslate.concatenate(translate);

            return finalTranslate;
        }

        @Override
        public Object getLeafLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_EAST;
        }

        @Override
        public Object getNodeHeightLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_NORTH;
        }

        @Override
        public Object getBranchLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_SOUTH;
        }
    };

    TreeDrawingOrientation DOWN = new TreeDrawingOrientation() {

        @Override
        public AffineTransform getTransform(Rectangle2D bounds) {
            //canonical coordinates are node positions:left->right heights:top->bottom
            // for horizontal left-to-right we want:
            // node positions: top->bottom
            // heights: right->left

            AffineTransform translate = AffineTransform.getTranslateInstance(-0.5, -0.5);
            AffineTransform flipY = AffineTransform.getScaleInstance(1, -1);
            AffineTransform scale = AffineTransform.getScaleInstance(bounds.getWidth(), bounds.getHeight());
            AffineTransform finalTranslate = AffineTransform.getTranslateInstance(bounds.getMinX() + bounds.getWidth() / 2.0, bounds.getMinY() + bounds.getHeight() / 2.0);
            finalTranslate.concatenate(scale);
            finalTranslate.concatenate(flipY);
            finalTranslate.concatenate(translate);

            return finalTranslate;
        }

        @Override
        public Object getLeafLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_NORTH;
        }

        @Override
        public Object getNodeHeightLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_WEST;
        }

        @Override
        public Object getBranchLabelAnchor() {
            return null;//TikzRenderingHints.VALUE_EAST;
        }

    };
}
