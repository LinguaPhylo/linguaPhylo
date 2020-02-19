package james.core.functions;

import james.graphicalModel.*;

import java.lang.reflect.Array;
import java.util.*;

public class Rep<U> extends DeterministicFunction<U[]> {

    private final String xParamName;
    private final String timesParamName;
    private Value<U> x;
    private Value<Integer> times;

    public Rep(@ParameterInfo(name = "x", description = "the element to replicate.") Value<U> x,
               @ParameterInfo(name = "times", description = "the standard deviation of the distribution.") Value<Integer> times) {

        this.x = x;
        if (x == null) throw new IllegalArgumentException("The mean value can't be null!");
        this.times = times;
        if (times == null) throw new IllegalArgumentException("The sd value can't be null!");

        xParamName = getParamName(0);
        timesParamName = getParamName(1);
    }

    @FunctionInfo(name = "rep", description = "The replication function. Takes a value and an integer representing the number of times to replicate the value. Returns a vector of the value repeated the specified number of times.")
    public Value<U[]> apply(Value<U> v, Value<Integer> times) {
        setParam("x", v);

        Class c = v.value().getClass();
        U[] array = (U[]) Array.newInstance(c, times.value());
        Arrays.fill(array, v.value());

        return new Value<>("rep(" + v.getId() + ", " + times.value() + ")", array, this);
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(xParamName, x);
        map.put(timesParamName, times);
        return map;
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(xParamName)) x = value;
        else if (paramName.equals(timesParamName)) times = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public Value<U[]> apply() {
        return apply(x, times);
    }
}