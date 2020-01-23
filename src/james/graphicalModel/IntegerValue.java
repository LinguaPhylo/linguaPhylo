package james.graphicalModel;

public class IntegerValue extends Value<Integer> {

    public IntegerValue(String id, Integer value) {
        super(id, value);
    }

    public IntegerValue(String id, Integer value, Function function) {
        super(id, value, function);
    }
}
