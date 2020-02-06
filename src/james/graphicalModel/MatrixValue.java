package james.graphicalModel;

/**
 * Created by adru001 on 2/02/20.
 */
public class MatrixValue extends Value<Double[][]> {

    public MatrixValue(String id, Double[][] value, DeterministicFunction<Double[][]> function) {
        super(id, value);
        this.function = function;
    }
}
