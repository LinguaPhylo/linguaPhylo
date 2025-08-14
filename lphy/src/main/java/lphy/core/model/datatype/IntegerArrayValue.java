package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import org.phylospec.types.Int;

import java.util.Arrays;
public class IntegerArrayValue extends VectorValue<Int> implements RangeElement {

    public IntegerArrayValue(String id, Int[] value) {
        super(id, value);
    }

    public IntegerArrayValue(String id, Int[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + Arrays.toString(value());
    }

    @Override
    public Integer[] range() {
        return Arrays.stream(value())
                .mapToInt(Int::getPrimitive)
                .boxed()                  // converts int -> Integer
                .toArray(Integer[]::new); // collect as Integer[]
    }
}
