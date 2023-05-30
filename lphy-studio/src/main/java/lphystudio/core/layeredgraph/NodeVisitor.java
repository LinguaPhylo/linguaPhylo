package lphystudio.core.layeredgraph;

import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.GenerativeDistribution;
import lphy.core.graphicalmodel.components.Value;

import java.awt.geom.Point2D;

/**TODO this is not used
 * Created by Alexei on 19/12/19.
 */
public interface NodeVisitor {

    void visitValue(Value value, Point2D p, Point2D q, int level);

    void visitGenEdge(GenerativeDistribution genDist, Point2D p, Point2D q, int level);

    void visitFunctionEdge(DeterministicFunction function, Point2D p, Point2D q, int level);
}
