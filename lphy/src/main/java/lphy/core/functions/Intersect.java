package lphy.core.functions;

import lphy.core.ParameterNames;
import lphy.graphicalModel.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Walter Xie
 */
public class Intersect<T> extends DeterministicFunction<T[]> {

    private final String Pa1st = ParameterNames.NoParamName0;
    private final String Pa2nd = ParameterNames.NoParamName1;

    public Intersect(@ParameterInfo(name = Pa1st, description = "set 1.") Value<T[]> a,
                     @ParameterInfo(name = Pa2nd, description = "set 2.") Value<T[]> b) {
        setInput(Pa1st, a);
        setInput(Pa2nd, b);
    }

    @Override
    @GeneratorInfo(name = "intersect",
            description = "A function to get intersection between two sets.")
    public Value<T[]> apply() {
        Value<T[]> a = (Value<T[]>)paramMap.get(Pa1st);
        Class<?> aTy = a.value().getClass().getComponentType();
        Value<T[]> b = (Value<T[]>)paramMap.get(Pa2nd);
        Class<?> bTy = b.value().getClass().getComponentType();

        if (!aTy.equals(bTy))
            throw new IllegalArgumentException("Must use the same type !");

        // Object[]
        List<T> intersect = Arrays.stream(a.value()).distinct()
                .filter(x -> Arrays.stream(b.value()).anyMatch(y -> y == x)).toList();
        return ValueUtils.createValue(intersect, this);
    }

}
