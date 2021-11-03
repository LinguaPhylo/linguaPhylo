package lphy.graphicalModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class VectorizedRandomVariable<T> extends RandomVariable<T[]> implements CompoundVector<T> {

    List<RandomVariable<T>> componentVariables = new ArrayList<>();

//    public VectorizedRandomVariable(String id, T[] value, GenerativeDistribution<T[]> generativeDistribution, List<GenerativeDistribution<T>> componentGenerators) {
//        super(id, value, generativeDistribution);
//
//        for (int i = 0; i < value.length; i++) {
//            RandomVariable<T> randomVariable = new RandomVariable<>(id + "." + i, value[i], componentGenerators != null ? componentGenerators.get(i) : null);
//            componentVariables.add(randomVariable);
//        }
//    }

    public VectorizedRandomVariable(String id, List<RandomVariable> componentVariables, GenerativeDistribution<T[]> generativeDistribution) {
        super(id, (T[])unwrapValues(componentVariables), generativeDistribution);

        for (int i = 0; i < value.length; i++) {
            this.componentVariables.add(componentVariables.get(i));
        }
    }

    private static Object[] unwrapValues(List<RandomVariable> values) {
        Object[] result = (Object[]) Array.newInstance(values.get(0).value().getClass(), values.size());
        for (int i = 0; i < result.length; i++) {
            result[i] = values.get(i).value();
        }
        return result;
    }

    public void setId(String id) {
        super.setId(id);
        for (int i = 0; i < componentVariables.size(); i++) {
            componentVariables.get(i).setId(id + VectorUtils.INDEX_SEPARATOR + i);
        }
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

    @Override
    public RandomVariable<T> getComponentValue(int i) {
        return componentVariables.get(i);
    }
}
