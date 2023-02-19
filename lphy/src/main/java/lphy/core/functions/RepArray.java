package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.util.Arrays;

public class RepArray<U> extends DeterministicFunction<U[]> {

    private final String xParamName = "arr";
    private final String timesParamName = "n";
    private Value<U[]> x;
    private Value<Integer> times;

    public RepArray(@ParameterInfo(name = xParamName, description = "1d-array to replicate.") Value<U[]> x,
                    @ParameterInfo(name = timesParamName, description = "the times to replicate array.") Value<Integer> times) {

        this.x = x;
        if (x == null) throw new IllegalArgumentException("The array can't be null!");
        U[] value = x.value();
        if (value == null || value.length < 1)
            throw new IllegalArgumentException("Must have at least 1 element in the array!");
        this.times = times;
        if (times == null) throw new IllegalArgumentException("The times can't be null!");
    }

    @GeneratorInfo(name = "repArray", description = "The replication function. " +
            "Takes an array and an integer representing the number of times to replicate the array. " +
            "Returns a vector of the value repeated the specified number of times.")
    public Value<U[]> apply() {
        U[] origArr = x.value();
        int t = times.value();
        U[] array = Arrays.copyOf(origArr, origArr.length * t);
        for (int i = 1; i < t; i++) {
            System.arraycopy(origArr, 0, array, origArr.length * i, origArr.length);
        }

        return new Value<>( null, array, this);
    }
}