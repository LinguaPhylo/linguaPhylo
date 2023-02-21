package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.util.Arrays;
import java.util.Collections;

import static lphy.core.ParameterNames.ArrayParamName;
import static lphy.core.ParameterNames.DecreasingParamName;

public class Sort<T> extends DeterministicFunction<T[]> {

    private final String sortByParamName = DecreasingParamName;
    private Value<T[]> x;
    private Value<Boolean> decreasing;

    public Sort(@ParameterInfo(name = ArrayParamName, description = "1d-array to sort.") Value<T[]> x,
                @ParameterInfo(name = sortByParamName,
            description = "sort the array by increasing (as default) or decreasing.",
                optional = true) Value<Boolean> decreasing) {

        this.x = x;
        if (x == null) throw new IllegalArgumentException("The array can't be null!");
        T[] value = x.value();
        if (value == null || value.length < 1)
            throw new IllegalArgumentException("Must have at least 1 element in the array!");
        if (decreasing == null)
            decreasing = new Value<>(null, false);
        this.decreasing = decreasing;
    }

    @GeneratorInfo(name = "sort", description = "The sort function sorts an array " +
            "by increasing (as default) or decreasing order.")
    public Value<T[]> apply() {
        T[] arr = x.value();
        if (decreasing.value())
            Arrays.sort(arr, Collections.reverseOrder());
        else
            Arrays.sort(arr);
        return new Value<>( null, arr, this);
    }
}