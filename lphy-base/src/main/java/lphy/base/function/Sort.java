package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.Arrays;
import java.util.Collections;

import static lphy.base.ParameterNames.ArrayParamName;
import static lphy.base.ParameterNames.DecreasingParamName;

public class Sort<T> extends DeterministicFunction<T[]> {

    public Sort(@ParameterInfo(name = ArrayParamName, description = "1d-array to sort.") Value<T[]> x,
                @ParameterInfo(name = DecreasingParamName,
            description = "sort the array by increasing (as default) or decreasing.",
                optional = true) Value<Boolean> decreasing) {

        if (x == null) throw new IllegalArgumentException("The array can't be null!");
        T[] value = x.value();
        if (value == null || value.length < 1)
            throw new IllegalArgumentException("Must have at least 1 element in the array!");

        setParam(ArrayParamName, x);
        if (decreasing != null)
            setParam(DecreasingParamName, decreasing);
    }

    @GeneratorInfo(name = "sort", description = "The sort function sorts an array " +
            "by increasing (as default) or decreasing order.")
    public Value<T[]> apply() {
        T[] arr = (T[]) getParams().get(ArrayParamName).value();
        Value<Boolean> decrVal = getParams().get(DecreasingParamName);
        if (decrVal != null && decrVal.value())
            Arrays.sort(arr, Collections.reverseOrder());
        else
            Arrays.sort(arr);
        return new Value<>( null, arr, this);
    }
}