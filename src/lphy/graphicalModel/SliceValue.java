package lphy.graphicalModel;

/**
 * Created by adru001 on 18/12/19.
 */
public class SliceValue<T> extends Value<T> {

    Value<T[]> slicedValue;
    int index;

    public SliceValue(int index, Value<T[]> slicedValue) {
        super(slicedValue.getId() + "." + index, slicedValue.value()[index]);
        this.slicedValue = slicedValue;
        this.index = index;
    }

    public Value<T[]> getSlicedValue() {
        return slicedValue;
    }

    public int getIndex() {
        return index;
    }
}
