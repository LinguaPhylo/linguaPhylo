package lphy.core.distributions;

import lphy.graphicalModel.*;
import lphy.util.RandomUtils;

import java.util.*;

public class Sample<U> implements GenerativeDistribution<U[]> {

    private final String xParamName = "arr";
    private final String sizeParamName = "size";
    private final String replParamName = "replace";
    private Value<U[]> x;
    private Value<Integer> size;
    private Value<Boolean> replace;

    Random random;

    public Sample(@ParameterInfo(name = xParamName, description = "1d-array to be sampled.") Value<U[]> x,
                  @ParameterInfo(name = sizeParamName, description = "the number of elements to choose.") Value<Integer> size,
                  @ParameterInfo(name = replParamName, description = "If replace is true, " +
                          "the same element can be sampled multiple times, if false (as default), " +
                          "it can only appear once in the result.",
                          optional = true) Value<Boolean> replace) {

        this.x = x;
        if (x == null) throw new IllegalArgumentException("The array can't be null!");
        U[] value = x.value();
        if (value == null || value.length < 1)
            throw new IllegalArgumentException("Must have at least 1 element in the array!");
        this.size = size;
        if (size == null) throw new IllegalArgumentException("The size can't be null!");
        if (size.value() <= 0 || size.value() > value.length)
            throw new IllegalArgumentException("Invalid size : " + size.value());
        if (replace == null)
            replace = new Value<>(null, false);
        this.replace = replace;

        random = RandomUtils.getJavaRandom();
    }

    @GeneratorInfo(name = "sample", description = "The sample function uniformly sample the subset of " +
            "a given size from an array of the elements either with or without the replacement.")
    public RandomVariable<U[]> sample() {
        List<U> origArr = new ArrayList<>(List.of(x.value()));
        int s = size.value();
        U[] array;
        if (replace.value()) {
            array = (U[]) new Object[s];
            int randomIndex;
            for (int i = 0; i < s; i++) {
                randomIndex = random.nextInt(origArr.size());
                array[i] = origArr.get(randomIndex);
            }
        } else { // no replacement
            Collections.shuffle(origArr, random);
            array = (U[]) origArr.stream().limit(s).toArray();
        }

        return new RandomVariable<>( null, array, this);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(xParamName, x);
            put(sizeParamName, size);
            put(replParamName, replace);
        }};
    }
}