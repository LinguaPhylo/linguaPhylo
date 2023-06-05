package lphy.core.model.component;

public interface ValueListener<T> {

    void valueSet(T oldValue, T newValue);
}
