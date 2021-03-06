package lphy.app;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.Value;

import java.awt.geom.Point2D;

/**
 * Created by adru001 on 19/12/19.
 */
public interface NodeVisitor {

    void visitValue(Value value, Point2D p, Point2D q, int level);

    void visitGenEdge(GenerativeDistribution genDist, Point2D p, Point2D q, int level);

    void visitFunctionEdge(DeterministicFunction function, Point2D p, Point2D q, int level);
}
