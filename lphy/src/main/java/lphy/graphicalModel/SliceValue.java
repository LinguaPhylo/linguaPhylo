package lphy.graphicalModel;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class SliceValue<T> extends Value<T> {

    Value<T[]> slicedValue;
    int index;

    public SliceValue(int index, Value<T[]> slicedValue) {
        // if id null, then set id null, to avoid null_0
        super(slicedValue.getId() == null ? null : slicedValue.getId() + VectorUtils.INDEX_SEPARATOR + index,
                slicedValue.value()[index]);
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
