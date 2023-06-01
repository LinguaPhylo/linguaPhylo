package lphy.core.model.components;

public interface ValueListener<T> {

    void valueSet(T oldValue, T newValue);
}
