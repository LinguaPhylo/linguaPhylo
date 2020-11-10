package lphy.graphicalModel;

public class VectorizedRandomVariable<T> extends RandomVariable<T[]> implements CompoundVector<T> {
    
    public VectorizedRandomVariable(String id, T[] value, GenerativeDistribution generativeDistribution) {
        super(id, value, generativeDistribution);
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
    public Value<T> getComponentValue(int i) {
        // TODO
        return null;
    }
}
