package lphy.base.distribution;

import lphy.base.ParameterNames;
import lphy.base.math.RandomUtils;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.GenerativeDistribution;
import lphy.core.model.component.RandomVariable;
import lphy.core.model.component.Value;
import lphy.core.model.component.VariableUtils;

import java.util.*;

public class Sample<T> implements GenerativeDistribution<T[]> {

    private final String replParamName = "replace";

    private Value<T[]> x;
    private Value<Integer> size;
    private Value<Boolean> replace;

    Random random;

    public Sample(@ParameterInfo(name = ParameterNames.ArrayParamName,
                          description = "1d-array to be sampled.") Value<T[]> x,
                  @ParameterInfo(name = ParameterNames.SizeParamName,
                          description = "the number of elements to choose.") Value<Integer> size,
                  @ParameterInfo(name = replParamName, description = "If replace is true, " +
                          "the same element can be sampled multiple times, if false (as default), " +
                          "it can only appear once in the result.",
                          optional = true) Value<Boolean> replace) {

        this.x = x;
        if (x == null) throw new IllegalArgumentException("The array can't be null!");
        this.size = size;
        if (size == null) throw new IllegalArgumentException("The size can't be null!");
        if (replace == null)
            replace = new Value<>(null, false);
        this.replace = replace;

        random = RandomUtils.getJavaRandom();
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(ParameterNames.ArrayParamName)) {
            T[] arr = x.value();
            if (arr == null || arr.length < 1)
                throw new IllegalArgumentException("Must have at least 1 element in the array! " + Arrays.toString(arr));
            x = value;
        }
        else if (paramName.equals(ParameterNames.SizeParamName)) {
            if (size.value() <= 0 || size.value() > x.value().length)
                throw new IllegalArgumentException("Invalid size : " + size.value());
            size = value;
        }
        else if (paramName.equals(replParamName)) {
            replace = value;
        }
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    @GeneratorInfo(name = "sample", description = "The sample function uniformly sample the subset of " +
            "a given size from an array of the elements either with or without the replacement.")
    public RandomVariable<T[]> sample() {
        List<T> origArr = Arrays.asList(x.value());
        int s = size.value();
        // use List to handle generic
        List<T> list2Arr;
        if (replace.value()) {
            list2Arr = new ArrayList<>();
            int randomIndex;
            for (int i = 0; i < s; i++) {
                randomIndex = random.nextInt(origArr.size());
                list2Arr.add( origArr.get(randomIndex) );
            }
        } else { // no replacement
            Collections.shuffle(origArr, random);
            list2Arr = origArr.stream().limit(s).toList();
        }
        System.out.println("Sample " + list2Arr.size() + " elements from the vector of " + origArr.size() +
                (replace.value() ? " with":" without") + " the replacement.");

        return VariableUtils.createRandomVariable( "S", list2Arr, this);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(ParameterNames.ArrayParamName, x);
            put(ParameterNames.SizeParamName, size);
            put(replParamName, replace);
        }};
    }
}