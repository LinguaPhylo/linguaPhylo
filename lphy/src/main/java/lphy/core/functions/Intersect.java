package lphy.core.functions;

import lphy.graphicalModel.*;

import java.util.Arrays;
import java.util.List;

import static lphy.core.ParameterNames.NoParamName0;
import static lphy.core.ParameterNames.NoParamName1;

/**
 * @author Walter Xie
 */
public class Intersect<T> extends DeterministicFunction<T[]> {

    public Intersect(@ParameterInfo(name = NoParamName0, description = "set 1.") Value<T[]> a,
                     @ParameterInfo(name = NoParamName1, description = "set 2.") Value<T[]> b) {
        setParam(NoParamName0, a);
        setParam(NoParamName1, b);
    }

    @Override
    @GeneratorInfo(name = "intersect", verbClause = "is", narrativeName = "intersection",
            description = "A function to get intersection between two sets.")
    public Value<T[]> apply() {
        T[] a = (T[]) getParams().get(NoParamName0).value();
        Class<?> aTy = a.getClass().getComponentType();
        T[] b = (T[]) getParams().get(NoParamName1).value();
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

        System.out.println("Intersect a vector "+ paramMap.get(NoParamName0).getCanonicalId() +
                " (" + a.length + ") with " + paramMap.get(NoParamName1).getCanonicalId() +
                " (" + b.length + "), the result length = " + intersect.size());
        return ValueUtils.createValue(intersect, this);
    }


}
