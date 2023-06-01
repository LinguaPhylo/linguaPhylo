package lphy.core.model.types;

public interface Vector<T> {

    Class<T> getComponentType();

    T getComponent(int i);

    int size();
}
