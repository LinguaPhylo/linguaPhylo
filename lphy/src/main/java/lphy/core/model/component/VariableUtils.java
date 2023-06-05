package lphy.core.model.component;

import java.util.List;

public class VariableUtils {


    /**
     * This is used to handle generic array, which has to be initiated as Object[].
     * @param list2Arr   generic type
     * @param generator  a {@link GenerativeDistribution}
     * @return a {@link RandomVariable} whose value is an array created from a generic type list.
     * @param <T>        Integer, Double, Boolean, ...
     */
    public static <T> RandomVariable<T[]> createRandomVariable(String name, List<T> list2Arr, GenerativeDistribution generator) {
        if (list2Arr.get(0) instanceof Integer)
            return new RandomVariable<>( name, list2Arr.toArray(Integer[]::new), generator);
        else if (list2Arr.get(0) instanceof Double)
            return new RandomVariable<>( name, list2Arr.toArray(Double[]::new), generator);
        else if (list2Arr.get(0) instanceof Boolean)
            return new RandomVariable<>( name, list2Arr.toArray(Boolean[]::new), generator);
        return new RandomVariable<>( name, list2Arr.toArray(), generator);
    }

}
