package lphystudio.core.layeredgraph;

import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.GenerativeDistribution;
import lphy.core.model.component.Value;

import java.awt.geom.Point2D;

/**TODO this is not used
 * Created by Alexei on 19/12/19.
 */
public interface NodeVisitor {

    void visitValue(Value value, Point2D p, Point2D q, int level);

    void visitGenEdge(GenerativeDistribution genDist, Point2D p, Point2D q, int level);

    void visitFunctionEdge(DeterministicFunction function, Point2D p, Point2D q, int level);
}
