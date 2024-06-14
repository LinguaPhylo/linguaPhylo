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

public class Union<T> extends DeterministicFunction<T[]> {
    public static final String firstSetName = "firstSet";
    public static final String secondSetName = "secondSet";

    public Union(
            @ParameterInfo(name = firstSetName, description = "the first set to put in the final set") Value<T[]> firstSet,
            @ParameterInfo(name = secondSetName, description = "the second set to put in the final set") Value<T[]> secondSet){
        if (firstSet == null) throw new IllegalArgumentException("The first set should not be null!");
        if (secondSet == null) throw new IllegalArgumentException("The second set should not be null!");
        setParam(firstSetName, firstSet);
        setParam(secondSetName, secondSet);
    }

    @GeneratorInfo(name = "setUnion", description = "Set the union of two given sets and remove all repeat elements.")
    @Override
    public Value<T[]> apply() {
        Set<T> firstSet = new HashSet<>(Arrays.asList(((Value<T[]>) getParams().get(firstSetName)).value()));
        Set<T> secondSet = new HashSet<>(Arrays.asList(((Value<T[]>) getParams().get(secondSetName)).value()));

        // combine the two sets
        firstSet.addAll(secondSet);

        // convert to array
        T[] resultArray = (T[]) Array.newInstance(((Value<T[]>) getParams().get(firstSetName)).value().getClass().getComponentType(), firstSet.size());
        firstSet.toArray(resultArray);

        return ValueCreator.createValue(resultArray, this);
    }
}
