package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.Arrays;
import java.util.Collections;

public class Sort<T> extends DeterministicFunction<T[]> {

    private final String sortByParamName = ParameterNames.DecreasingParamName;

    public Sort(@ParameterInfo(name = ParameterNames.ArrayParamName, description = "1d-array to sort.") Value<T[]> x,
                @ParameterInfo(name = sortByParamName,
            description = "sort the array by increasing (as default) or decreasing.",
                optional = true) Value<Boolean> decreasing) {

        if (x == null) throw new IllegalArgumentException("The array can't be null!");
        T[] value = x.value();
        if (value == null || value.length < 1)
            throw new IllegalArgumentException("Must have at least 1 element in the array!");

        setParam(ParameterNames.ArrayParamName, x);
        if (decreasing != null)
            setParam(sortByParamName, decreasing);
    }

    @GeneratorInfo(name = "sort", description = "The sort function sorts an array " +
            "by increasing (as default) or decreasing order.")
    public Value<T[]> apply() {
        T[] arr = (T[]) getParams().get(ParameterNames.ArrayParamName).value();
        Value<Boolean> decrVal = getParams().get(sortByParamName);
        if (decrVal != null && decrVal.value())
            Arrays.sort(arr, Collections.reverseOrder());
        else
            Arrays.sort(arr);
        return new Value<>( null, arr, this);
    }
}