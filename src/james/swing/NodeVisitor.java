package james.swing;

import james.graphicalModel.Function;
import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.Value;

import java.awt.geom.Point2D;

/**
 * Created by adru001 on 19/12/19.
 */
public interface NodeVisitor {

    void visitValue(Value value, Point2D p, Point2D q);

    void visitGenEdge(GenerativeDistribution genDist, Point2D p, Point2D q);

    void visitFunctionEdge(Function function, Point2D p, Point2D q);
}
