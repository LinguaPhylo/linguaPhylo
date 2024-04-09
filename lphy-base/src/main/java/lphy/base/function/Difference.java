package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Difference<T> extends DeterministicFunction {
    public static final String wholeSetName = "whole set";
    public static final String subSetName = "sub set";

    public Difference(
            @ParameterInfo(name = wholeSetName, description = "the whole set contains all the elements") Value<T[]> wholeSet,
            @ParameterInfo(name = subSetName, description = "the subset that we want to subtract from the whole set") Value<T[]> subSet){
        if (wholeSet == null || subSet == null) throw new IllegalArgumentException("The sets can't be null!");
        if (! new HashSet<>(Arrays.asList(wholeSet.value())).containsAll(new HashSet<>(Arrays.asList(subSet.value())))) {
            throw new IllegalArgumentException("The sets can't be null!");
        }
        setParam(wholeSetName, wholeSet);
        setParam(subSetName,  subSet);
    }

    @GeneratorInfo(name = "setDifference", description = "Get the difference of two arrays. First parameter is the whole " +
            "set, second parameter is the sub set.")
    @Override
    public Value<T[]> apply() {
        Set<T> set1 = new HashSet<>(Arrays.asList(((Value<T[]>) getParams().get(wholeSetName)).value()));
        Set<T> set2 = new HashSet<>(Arrays.asList(((Value<T[]>) getParams().get(subSetName)).value()));

        Set<T> difference = new HashSet<>(set1);

        // get difference of two sets
        difference.removeAll(set2);

        // convert to T[]
        T[] differenceSet = (T[]) Array.newInstance(((Value<T[]>) getParams().get(wholeSetName)).value().getClass().getComponentType(), difference.size());
        difference.toArray(differenceSet);

        return new Value<>(null, differenceSet);
    }
}
