package lphy.graphicalModel;

import lphy.core.distributions.VectorizedDistribution;

import java.util.Arrays;

public class VectorizedRandomVariable<T> extends RandomVariable<T[]> implements Vector<T> {

    public VectorizedRandomVariable(String id, T[] value, VectorizedDistribution generativeDistribution) {
        super(id, value, generativeDistribution);
    }

    public GenerativeDistribution getComponentDistribution(int i) {
        return ((VectorizedDistribution)getGenerativeDistribution()).getBaseDistribution(i);
    }

    @Override
    public Class<T> getComponentType() {
        return (Class<T>)value()[0].getClass();
    }

    @Override
    public T getComponent(int i) {
        return value()[0];
    }

    @Override
    public int size() {
        return value().length;
    }
}
