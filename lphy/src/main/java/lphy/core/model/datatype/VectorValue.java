package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;

import java.util.Arrays;

public class VectorValue<T> extends Value<T[]> implements Vector<T> {

    public VectorValue(String id, Object valueArray, Class<T> componentClass) {
        super(id, (T[])valueArray);
    }

    public VectorValue(String id, T[] value) {
        super(id, value);
    }

    public VectorValue(String id, T[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public VectorValue(String id, Object valueArray, Class<T> componentClass, DeterministicFunction function) {
        super(id, (T[])valueArray, function);
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

//    @Override
//    public List<T> getElements() {
//        return Arrays.stream(value()).toList();
//    }
//
//    @Override
//    public T get(int i) {
//        return value()[i];
//    }
}
