package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.parser.graphicalmodel.ValueCreator;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Difference<T> extends DeterministicFunction {
    public static final String firstSetName = "wholeSet";
    public static final String secondSetName = "subSet";

    public Difference(
            @ParameterInfo(name = firstSetName, description = "the whole set contains all the elements") Value<T[]> wholeSet,
            @ParameterInfo(name = secondSetName, description = "the subset that we want to subtract from the whole set") Value<T[]> subSet){
        if (wholeSet == null || subSet == null) throw new IllegalArgumentException("The sets can't be null!");
        setParam(firstSetName, wholeSet);
        setParam(secondSetName,  subSet);
    }

    @GeneratorInfo(name = "setDifference", description = "Get the difference of two arrays. First parameter is the whole " +
            "set, second parameter is the sub set. If first array contains the second one, then return to the difference. If the " +
            "second one contains elements does not exist in the first array, then ignore them.")
    @Override
    public Value<T[]> apply() {
        Set<T> set1 = new HashSet<>(Arrays.asList(((Value<T[]>) getParams().get(firstSetName)).value()));
        Set<T> set2 = new HashSet<>(Arrays.asList(((Value<T[]>) getParams().get(secondSetName)).value()));

        Set<T> difference = new HashSet<>(set1);

        // get difference of two sets
        difference.removeAll(set2);

        // convert to T[]
        T[] differenceSet = (T[]) Array.newInstance(((Value<T[]>) getParams().get(firstSetName)).value().getClass().getComponentType(), difference.size());
        difference.toArray(differenceSet);

        return ValueCreator.createValue(differenceSet,this);
    }
}
