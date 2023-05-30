package lphy.core.graphicalmodel.components;

public interface ValueListener<T> {

    void valueSet(T oldValue, T newValue);
}
