package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.Vector;

import java.util.Arrays;

public class VectorValue<T> extends Value<T[]> implements Vector<T> {

    public VectorValue(String id, T[] value) {
        super(id, value);
    }

    public VectorValue(String id, T[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + Arrays.toString(value());
    }

    @Override
    public Class<T> getComponentType() {
        return (Class<T>)value()[0].getClass();
    }

    @Override
    public T getComponent(int i) {
        return value()[i];
    }

    @Override
    public int size() {
        return value().length;
    }
}
