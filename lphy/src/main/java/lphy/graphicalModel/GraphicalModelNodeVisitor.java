package lphy.graphicalModel;

/**
 * Created by Alexei Drummond on 24/01/20.
 */
public interface GraphicalModelNodeVisitor {
    void visitValue(Value value);

    void visitGenerator(Generator g);
}
