package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.parser.graphicalmodel.ValueCreator;

import java.lang.reflect.Array;
import java.util.*;

public class Union<T> extends DeterministicFunction<T[]> {
    public static final String firstSetName = "firstSet";
    public static final String secondSetName = "secondSet";
    public static final String includeRepeatsName = "includeRepeats";

    public Union(
            @ParameterInfo(name = firstSetName, description = "the first set to put in the final set") Value<T[]> firstSet,
            @ParameterInfo(name = secondSetName, description = "the second set to put in the final set") Value<T[]> secondSet,
            @ParameterInfo(name = includeRepeatsName, description = "whether include the repeat elements, default false", optional = false) Value<Boolean> includeRepeats){
        if (firstSet == null) throw new IllegalArgumentException("The first set should not be null!");
        if (secondSet == null) throw new IllegalArgumentException("The second set should not be null!");
        setParam(firstSetName, firstSet);
        setParam(secondSetName, secondSet);
        setParam(includeRepeatsName, includeRepeats);
    }

    @GeneratorInfo(name = "setUnion", description = "Set the union of all given sets. Default remove all repeat elements.")
    @Override
    public Value<T[]> apply() {
        T[] firstSet = (T[]) getParams().get(firstSetName).value();
        T[] secondSet = (T[]) getParams().get(secondSetName).value();

        Value<Boolean> includeRepeatsValue = getParams().get(includeRepeatsName);
        Boolean includeRepeats = (includeRepeatsValue != null && includeRepeatsValue.value() != null) ? includeRepeatsValue.value() : false;

        // add both set info
        List<T> combinedList = new ArrayList<>();
        combinedList.addAll(Arrays.asList(firstSet));
        combinedList.addAll(Arrays.asList(secondSet));

        if ( !includeRepeats){
            //remove repeats
            Set<T> uniqueSet = new HashSet<>(combinedList);
            combinedList = new ArrayList<>(uniqueSet);
        }

        // convert to array
        T[] resultArray = (T[]) Array.newInstance(firstSet.getClass().getComponentType(), combinedList.size());
        combinedList.toArray(resultArray);

        return ValueCreator.createValue(resultArray, this);
    }
}
