package lphy.core.vectorization;

import lphy.core.model.component.Value;
import lphy.core.model.datatype.Vector;

public interface CompoundVector<T> extends Vector<T> {

    Value<T> getComponentValue(int i);
}
