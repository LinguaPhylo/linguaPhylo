package lphy;

import java.awt.geom.Point2D;

public interface TimeTreeNodeVisitor {

    void visitNode(TimeTreeNode node, Point2D nodePoint, Point2D parentPoint);
}
