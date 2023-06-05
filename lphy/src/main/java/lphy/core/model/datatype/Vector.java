package lphy.core.model.datatype;

public interface Vector<T> {

    Class<T> getComponentType();

    T getComponent(int i);

    int size();
}
