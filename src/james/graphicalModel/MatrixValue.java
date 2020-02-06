package james.graphicalModel;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by adru001 on 2/02/20.
 */
public class MatrixValue extends Value<RealMatrix> {

    public MatrixValue(String id, double[][] value, DeterministicFunction<RealMatrix> function) {
        super(id, new Array2DRowRealMatrix(value));
        this.function = function;
    }
}
