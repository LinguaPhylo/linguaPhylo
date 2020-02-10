package james.graphicalModel.types;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.Value;
import james.graphicalModel.Viewable;
import james.app.RealMatrixEditor;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import javax.swing.*;

/**
 * Created by adru001 on 2/02/20.
 */
public class MatrixValue extends Value<RealMatrix> implements Viewable {

    public MatrixValue(String id, double[][] value, DeterministicFunction<RealMatrix> function) {
        super(id, new Array2DRowRealMatrix(value), function);
    }

    public JComponent getViewer() {

        JComponent realMatrixComponent = new RealMatrixEditor(value(), false);
        realMatrixComponent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10,10,10,10), getId()));
        return realMatrixComponent;
    }

}
