package lphy.core.model;

import lphy.core.model.components.Generator;
import lphy.core.model.components.Value;

/**
 * Created by Alexei Drummond on 24/01/20.
 */
public interface GraphicalModelNodeVisitor {
    void visitValue(Value value);

    void visitGenerator(Generator g);
}
