package lphy.core.vectorization;

import lphy.core.model.components.Value;
import lphy.core.model.types.Vector;

public interface CompoundVector<T> extends Vector<T> {

    Value<T> getComponentValue(int i);
}
