package lphy.graphicalModel.types;

import lphy.graphicalModel.CompoundVector;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.Vector;

import java.lang.reflect.Array;
import java.util.Arrays;

public class CompoundVectorValue<T> extends Value<T[]> implements CompoundVector<T> {

    Value<T>[] internalValues;

    public CompoundVectorValue(String id, Value<T>[] value) {

        super(id, (T[])unwrapValues(value));
        internalValues = value;
    }

    private static Object[] unwrapValues(Value[] value) {
        Object[] result = (Object[])Array.newInstance(value[0].value().getClass(), value.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = value[i].value();
        }
        return result;
    }

    public CompoundVectorValue(String id, Value<T>[] value, DeterministicFunction function) {
        super(id, (T[])unwrapValues(value), function);
        internalValues = value;
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + Arrays.toString(value());
    }

    @Override
    public Class<T> getComponentType() {
        return (Class<T>)value()[0].getClass();
    }

    public Value<T> getComponentValue(int i) {
        return internalValues[i];
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
