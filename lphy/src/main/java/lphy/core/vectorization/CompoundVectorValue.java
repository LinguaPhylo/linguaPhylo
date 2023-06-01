package lphy.core.vectorization;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.Value;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompoundVectorValue<T> extends Value<T[]> implements CompoundVector<T> {

    List<Value<T>> componentValues = new ArrayList<>();

    public CompoundVectorValue(String id, List<Value> values, DeterministicFunction function) {
        super(id, (T[])unwrapValues(values), function);
        for (Value value : values) {
            componentValues.add(value);
        }
    }

    private static Object[] unwrapValues(List<Value> values) {
        Object[] result = (Object[]) Array.newInstance(values.get(0).value().getClass(), values.size());
        for (int i = 0; i < result.length; i++) {
            result[i] = values.get(i).value();
        }
        return result;
    }

    public void setId(String id) {
        super.setId(id);
        for (int i = 0; i < componentValues.size(); i++) {
            componentValues.get(i).setId(id + VectorUtils.INDEX_SEPARATOR + i);
        }
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + Arrays.toString(value());
    }

    @Override
    public Class<T> getComponentType() {
        return (Class<T>)value()[0].getClass();
    }

    public Value<T> getComponentValue(int i) {
        return componentValues.get(i);
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
