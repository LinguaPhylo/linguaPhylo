package lphy.core.graphicalmodel.types;

public interface Vector<T> {

    Class<T> getComponentType();

    T getComponent(int i);

    int size();
}
