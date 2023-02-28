package lphy.core.functions;

import lphy.graphicalModel.*;

import java.util.Arrays;

import static lphy.core.ParameterNames.NoParamName0;
import static lphy.core.ParameterNames.NoParamName1;

/**
 * @author Walter Xie
 */
public class ConcatArray<T> extends DeterministicFunction<T[]> {


    public ConcatArray(@ParameterInfo(name = NoParamName0, description = "array 1.") Value<T[]> a,
                       @ParameterInfo(name = NoParamName1, description = "array 2.") Value<T[]> b) {
        setParam(NoParamName0, a);
        setParam(NoParamName1, b);
    }

    @Override
    @GeneratorInfo(name = "concatArray", verbClause = "is concatenated by", narrativeName = "vector",
            description = "A function to concatenate two arrays into one.")
    public Value<T[]> apply() {
        T[] a = (T[])paramMap.get(NoParamName0).value();
        Class<?> aTy = a.getClass().getComponentType();
        T[] b = (T[])paramMap.get(NoParamName1).value();
        Class<?> bTy = b.getClass().getComponentType();

        if (!aTy.equals(bTy))
            throw new IllegalArgumentException("concatArray function must use the same type between arguments ! " +
                    aTy + " != " + bTy);

        // Object[]
        T[] array = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, array, a.length, b.length);

        System.out.println("Concatenate two vectors sized at " + a.length + " and " + b.length +
                " into one, final length = " + array.length);
        return ValueUtils.createValue(array, this);
    }

}
