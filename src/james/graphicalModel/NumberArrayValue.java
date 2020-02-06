package james.graphicalModel;

public class NumberArrayValue<U extends Number> extends Value<U[]> {

    public NumberArrayValue(String id, U[] values) {
        super(id, values);
    }
}
