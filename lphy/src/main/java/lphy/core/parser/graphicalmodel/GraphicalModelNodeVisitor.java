package lphy.core.parser.graphicalmodel;

import lphy.core.model.Generator;
import lphy.core.model.Value;

/**
 * Created by Alexei Drummond on 24/01/20.
 */
public interface GraphicalModelNodeVisitor {
    void visitValue(Value value);

    void visitGenerator(Generator g);
}
