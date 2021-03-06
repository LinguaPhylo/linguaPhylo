package lphy.graphicalModel;

public interface ValueListener<T> {

    void valueSet(T oldValue, T newValue);
}
