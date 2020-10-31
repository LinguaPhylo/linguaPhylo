package lphy.graphicalModel;

import lphy.app.HasComponentView;
import lphy.app.VectorComponent;

import javax.swing.*;

public class VectorizedRandomVariable<T> extends RandomVariable<T[]> implements Vector<T> {
    
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
}
