package lphy.graphicalModel;

public interface CompoundVector<T> extends Vector<T> {

    Value<T> getComponentValue(int i);
}
