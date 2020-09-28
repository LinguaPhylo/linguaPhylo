package lphy.core.distributions;

import lphy.graphicalModel.*;

import java.util.*;

public class RandomBooleanArray implements GenerativeDistribution<Boolean[]> {

    private final String lengthParamName;
    private final String hammingWeightParamName;
    private Value<Integer> length;
    private Value<Integer> hammingWeight;

    public RandomBooleanArray(@ParameterInfo(name="length", description="the length of the boolean array to be generated.") Value<Integer> length,
                              @ParameterInfo(name="hammingWeight", description="the number of true values in the boolean array.") Value<Integer> hammingWeight) {
        this.length = length;
        this.hammingWeight = hammingWeight;
        lengthParamName = getParamName(0);
        hammingWeightParamName = getParamName(1);
    }

    @GeneratorInfo(name="RandomBooleanArray", description="Samples a random boolean array of given length and given hamming weight. " +
            "The hamming weight is the number of true values in the array and must be less than or equal to the length.")
    public RandomVariable<Boolean[]> sample() {
        List<Boolean> bools = new ArrayList<>();

        int weight = hammingWeight.value();

        if (weight > length.value()) {
            weight = length.value();
            System.err.println("WARNING: hammingWeight was greater than length in " + this.getClass().getSimpleName() + "! Will produce an array of true values.");
        }

        for (int i = 0; i < weight; i++) {
            bools.add(true);
        }
        while (bools.size() < length.value()) {
            bools.add(false);
        }

        Collections.shuffle(bools);
        Boolean[] array = new Boolean[bools.size()];

        return new RandomVariable<>("x", bools.toArray(array), this);
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(lengthParamName, length);
        map.put(hammingWeightParamName, hammingWeight);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(lengthParamName)) length = value;
        else if (paramName.equals(hammingWeightParamName)) hammingWeight = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }
}
