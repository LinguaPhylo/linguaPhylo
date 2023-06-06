package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.Arrays;
import java.util.List;

/**
 * @author Walter Xie
 */
public class Intersect<T> extends DeterministicFunction<T[]> {

    public Intersect(@ParameterInfo(name = ParameterNames.NoParamName0, description = "set 1.") Value<T[]> a,
                     @ParameterInfo(name = ParameterNames.NoParamName1, description = "set 2.") Value<T[]> b) {
        // this adds value to output, so no arg name works when click sample button
        setInput(ParameterNames.NoParamName0, a);
        setInput(ParameterNames.NoParamName1, b);
    }

    @Override
    @GeneratorInfo(name = "intersect", verbClause = "is", narrativeName = "intersection",
            description = "A function to get intersection between two sets.")
    public Value<T[]> apply() {
        T[] a = (T[]) getParams().get(ParameterNames.NoParamName0).value();
        Class<?> aTy = a.getClass().getComponentType();
        T[] b = (T[]) getParams().get(ParameterNames.NoParamName1).value();
        Class<?> bTy = b.getClass().getComponentType();

        if (!aTy.equals(bTy))
            throw new IllegalArgumentException("Must use the same type !");

//        Set<String> intersection = set1.stream()
//                .filter(set2::contains)
//                .collect(Collectors.toSet());
        List<T> intersect;
        if (a.length < b.length) // stream b if it is larger
            intersect = Arrays.stream(b).distinct()
                    .filter(x -> Arrays.asList(a).contains(x)).toList();
        else
            intersect= Arrays.stream(a).distinct()
                .filter(x -> Arrays.asList(b).contains(x)).toList();

        System.out.println("Intersect a vector "+ paramMap.get(ParameterNames.NoParamName0).getCanonicalId() +
                " (" + a.length + ") with " + paramMap.get(ParameterNames.NoParamName1).getCanonicalId() +
                " (" + b.length + "), the result length = " + intersect.size());
        return ValueUtils.createValue(intersect, this);
    }
}
