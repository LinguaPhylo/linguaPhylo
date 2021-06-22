package lphy.graphicalModel;

public interface Vector<T> {

    Class<T> getComponentType();

    T getComponent(int i);

    int size();
}
