package james.graphicalModel;

import james.core.functions.JukesCantor;

/**
 * Created by adru001 on 2/02/20.
 */
public class MatrixValue extends Value<Double[][]> {

    public MatrixValue(String id, Double[][] value, Function<Double, Double[][]> function) {
        super(id, value);
        this.function = function;
    }
}
