package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.Value;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

public class Rep<U> extends DeterministicFunction<U[]> {

    private final String xParamName = "element";
    private Value<U> x;
    private Value<Integer> times;

    public Rep(@ParameterInfo(name = xParamName, description = "the element to replicate.") Value<U> x,
               @ParameterInfo(name = ParameterNames.TimesParamName, description = "the standard deviation of the distribution.") Value<Integer> times) {

        this.x = x;
        if (x == null) throw new IllegalArgumentException("The element can't be null!");
        this.times = times;
        if (times == null) throw new IllegalArgumentException("The times can't be null!");
    }

    @GeneratorInfo(name = "rep", description = "The replication function. Takes a value and an integer representing the number of times to replicate the value. Returns a vector of the value repeated the specified number of times.")
    public Value<U[]> apply(Value<U> v, Value<Integer> times) {

        Class c = v.value().getClass();
        U[] array = (U[]) Array.newInstance(c, times.value());
        Arrays.fill(array, v.value());

        return new Value<>( array, this);
    }

    public java.util.Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(xParamName, x);
        map.put(ParameterNames.TimesParamName, times);
        return map;
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(xParamName)) x = value;
        else if (paramName.equals(ParameterNames.TimesParamName)) times = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public Value<U[]> apply() {
        return apply(x, times);
    }
}