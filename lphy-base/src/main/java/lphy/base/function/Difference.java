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
import java.util.logging.Logger;

public class Difference<T> extends DeterministicFunction<T[]> {
    public static final String firstSetName = "mainSet";
    public static final String secondSetName = "excludeSet";
    private static final Logger LOGGER = Logger.getLogger(Difference.class.getName());

    public Difference(
            @ParameterInfo(name = firstSetName, description = "the primary set of elements from which to exclude") Value<T[]> mainSet,
            @ParameterInfo(name = secondSetName, description = "the set of elements to be excluded from the main set") Value<T[]> excludeSet){
        if (mainSet == null) throw new IllegalArgumentException("The main set can't be null!");
        if (excludeSet == null) throw new IllegalArgumentException("The exclude set can't be null!");
        if (Arrays.equals(mainSet.value(), excludeSet.value())) {
            LOGGER.warning("The difference set is empty because the main set is equal to the exclude set.");
        }
        setParam(firstSetName, mainSet);
        setParam(secondSetName,  excludeSet);
    }

    @GeneratorInfo(name = "setDifference", description = "Computes the difference between two arrays. The first parameter is the main set, and the second parameter is the exclude set to subtract from the whole set. Elements in the second set not found in the first set are ignored.")
    @Override
    public Value<T[]> apply() {
        Set<T> set1 = new HashSet<>(Arrays.asList(((Value<T[]>) getParams().get(firstSetName)).value()));
        Set<T> set2 = new HashSet<>(Arrays.asList(((Value<T[]>) getParams().get(secondSetName)).value()));

        // Calculate the difference
        set1.removeAll(set2);

        // Convert to T[]
        T[] differenceSet = (T[]) Array.newInstance(((Value<T[]>) getParams().get(firstSetName)).value().getClass().getComponentType(), set1.size());
        set1.toArray(differenceSet);

        return ValueCreator.createValue(differenceSet, this);
    }
}
