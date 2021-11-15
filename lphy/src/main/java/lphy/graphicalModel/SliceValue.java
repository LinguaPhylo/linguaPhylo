package lphy.graphicalModel;

import lphy.core.narrative.Narrative;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class SliceValue<T> extends Value<T> {

    Value<T[]> slicedValue;
    int index;

    public SliceValue(int index, Value<T[]> slicedValue) {
        super(slicedValue.getId() + VectorUtils.INDEX_SEPARATOR + index, slicedValue.value()[index]);
        this.slicedValue = slicedValue;
        this.index = index;
    }

    /**
     * @return true if this value is sliced from a random value
     */
    public boolean isRandom() {
        return slicedValue.isRandom();
    }

    public Value<T[]> getSlicedValue() {
        return slicedValue;
    }

    public int getIndex() {
        return index;
    }
}
