package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.Value;
import lphy.core.model.component.ValueUtils;

import java.util.Arrays;

/**
 * @author Walter Xie
 */
public class ConcatArray<T> extends DeterministicFunction<T[]> {


    public ConcatArray(@ParameterInfo(name = ParameterNames.NoParamName0, description = "array 1.") Value<T[]> a,
                       @ParameterInfo(name = ParameterNames.NoParamName1, description = "array 2.") Value<T[]> b) {
        // this adds value to output, so no arg name works when click sample button
        setInput(ParameterNames.NoParamName0, a);
        setInput(ParameterNames.NoParamName1, b);
    }

    @Override
    @GeneratorInfo(name = "concatArray", verbClause = "is concatenated by", narrativeName = "vector",
            description = "A function to concatenate two arrays into one.")
    public Value<T[]> apply() {
        T[] a = (T[])paramMap.get(ParameterNames.NoParamName0).value();
        Class<?> aTy = a.getClass().getComponentType();
        T[] b = (T[])paramMap.get(ParameterNames.NoParamName1).value();
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
