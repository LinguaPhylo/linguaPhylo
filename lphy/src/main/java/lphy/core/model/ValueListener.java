package lphy.core.model;

public interface ValueListener<T> {

    void valueSet(T oldValue, T newValue);
}
