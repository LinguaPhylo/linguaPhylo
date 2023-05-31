package lphy.core.vectorization;

import lphy.core.graphicalmodel.components.Value;
import lphy.core.graphicalmodel.types.Vector;

public interface CompoundVector<T> extends Vector<T> {

    Value<T> getComponentValue(int i);
}
