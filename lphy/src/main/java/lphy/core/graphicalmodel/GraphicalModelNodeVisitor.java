package lphy.core.graphicalmodel;

import lphy.core.graphicalmodel.components.Generator;
import lphy.core.graphicalmodel.components.Value;

/**
 * Created by Alexei Drummond on 24/01/20.
 */
public interface GraphicalModelNodeVisitor {
    void visitValue(Value value);

    void visitGenerator(Generator g);
}
