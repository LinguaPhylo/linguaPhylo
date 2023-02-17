package lphy.core.functions;

import lphy.graphicalModel.*;

import java.util.Arrays;

/**
 * @author Walter Xie
 */
public class Intersect<T> extends DeterministicFunction<T[]> {


    public Intersect(@ParameterInfo(name = "0", description = "the set.") Value<T[]> a,
                     @ParameterInfo(name = "1", description = "the set.") Value<T[]> b) {
        setInput("0", a);
        setInput("1", b);
    }

    @Override
    @GeneratorInfo(name = "intersect",
            description = "A function to take intersection between two sets.")
    public Value<T[]> apply() {
        Value<T[]> a = (Value<T[]>)paramMap.get("0");
        Class<?> aTy = a.value().getClass().getComponentType();
        Value<T[]> b = (Value<T[]>)paramMap.get("1");
        Class<?> bTy = b.value().getClass().getComponentType();

        if (!aTy.equals(bTy))
            throw new IllegalArgumentException("Must use the same type !");

        // Object[]
        T[] intsect = (T[]) Arrays.stream(a.value()).distinct()
                .filter(x -> Arrays.stream(b.value()).anyMatch(y -> y == x)).
                toArray();

//            return ValueUtils.createValue(intsect.toArray(Integer[]::new), this);
//        if (intsect[0] instanceof Double)
//            return ValueUtils.createValue((Double[]) intsect, this);
//        if (intsect[0] instanceof Boolean)
//            return ValueUtils.createValue((Boolean[]) intsect, this);
        return ValueUtils.createValue(intsect, this);
    }

}
