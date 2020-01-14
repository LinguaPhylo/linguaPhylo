package james;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * @author Alexei Drummond
 */
public interface BranchStyle {

    public Shape getBranchShape(Point2D canonicalChildPoint2D, Point2D canonicalParentPoint2D);

    public Point2D getCanonicalBranchLabelPoint2D(Point2D canonicalChildPoint2D, Point2D canonicalParentPoint2D);

    BranchStyle LINE = new BranchStyle() {
        @Override
        public Shape getBranchShape(Point2D canonicalChildPoint2D, Point2D canonicalParentPoint2D) {
            return new Line2D.Double(canonicalChildPoint2D, canonicalParentPoint2D);
        }

        @Override
        public Point2D getCanonicalBranchLabelPoint2D(Point2D canonicalChildPoint2D, Point2D canonicalParentPoint2D) {
            return new Point2D.Double((canonicalChildPoint2D.getX() + canonicalParentPoint2D.getX()) / 2.0,
                    (canonicalChildPoint2D.getY() + canonicalParentPoint2D.getY()) / 2.0);
        }
    };

    BranchStyle SQUARE = new BranchStyle() {
        @Override
        public Shape getBranchShape(Point2D canonicalChildPoint2D, Point2D canonicalParentPoint2D) {
            Path2D path = new Path2D.Double();
            path.moveTo(canonicalChildPoint2D.getX(), canonicalChildPoint2D.getY());
            path.lineTo(canonicalChildPoint2D.getX(), canonicalParentPoint2D.getY());
            path.lineTo(canonicalParentPoint2D.getX(), canonicalParentPoint2D.getY());
            return path;
        }

        @Override
        public Point2D getCanonicalBranchLabelPoint2D(Point2D canonicalChildPoint2D, Point2D canonicalParentPoint2D) {
            return new Point2D.Double(canonicalChildPoint2D.getX(), (canonicalChildPoint2D.getY() + canonicalParentPoint2D.getY()) / 2.0);
        }
    };
}
