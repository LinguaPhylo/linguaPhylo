package lphy.base.distribution;

import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.simulator.RandomUtils;

import java.util.*;

public class RandomBooleanArray implements GenerativeDistribution<Boolean[]> {

    private static final String lengthParamName = "length";
    private static final String hammingWeightParamName = "hammingWeight";
    private Value<Integer> length;
    private Value<Integer> hammingWeight;

    private Random random;

    public RandomBooleanArray(@ParameterInfo(name=lengthParamName, description="the length of the boolean array to be generated.") Value<Integer> length,
                              @ParameterInfo(name=hammingWeightParamName, description="the number of true values in the boolean array.") Value<Integer> hammingWeight) {
        this.length = length;
        this.hammingWeight = hammingWeight;

        this.random = RandomUtils.getJavaRandom();
    }

    @GeneratorInfo(name="RandomBooleanArray",
            category = GeneratorCategory.PRIOR, examples = {"simpleRandomLocalClock2.lphy"},
            description="Samples a random boolean array of given length and given hamming weight. " +
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

        Collections.shuffle(bools, random);
        Boolean[] array = new Boolean[bools.size()];

        return new RandomVariable<>("x", bools.toArray(array), this);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(lengthParamName, length);
            put(hammingWeightParamName, hammingWeight);
        }};
    }

    //TODO cannot work with Number. Perhaps change to setParam
    public void setLength(int length) {
        this.length.setValue(length);
    }

    public void setHammingWeight(int hammingWeight) {
        this.hammingWeight.setValue(hammingWeight);
    }
}
