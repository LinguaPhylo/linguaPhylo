package lphy.base.functions;

import lphy.core.graphicalmodel.components.*;
import lphy.core.vectorization.CompoundVectorValue;

import java.util.*;

public class Unique<T> extends DeterministicFunction<T[]> {

    public static final String argParamName = "arg";

    public Unique(@ParameterInfo(name = argParamName, verb="of",
            description = "the string array to find its unique set.") Value<?> x) {
        setParam(argParamName, x);
    }

    @GeneratorInfo(name="unique", verbClause = "provides", description = "the unique set of the array")
    public Value<T[]> apply() {
        Value<?> v = getParams().get(argParamName);
        Set<T> uniqObj = new LinkedHashSet<>();
        List<Value> uniqVal = new ArrayList<>();

        if (v instanceof CompoundVectorValue) {
            CompoundVectorValue cvv = (CompoundVectorValue) v;
            for (int i = 0; i < cvv.size(); i++) {
                Value val = cvv.getComponentValue(i);
                T obj = (T) Objects.requireNonNull(val).value();
                if (!uniqObj.contains(obj)) {
                    uniqObj.add(obj);
                    uniqVal.add(val);
                }
            }
            return new CompoundVectorValue<>(null, uniqVal, this);
        } else {
            return ValueUtils.createValue(uniqObj.toArray(), this);
        }

//        StringArray strArr = Objects.requireNonNull(v).value();
//        Set<String> uniqArr;
//        if (strArr.getClass().isArray()) {
//            String[] arr = strArr.value();
//            uniqArr = new LinkedHashSet<>(Arrays.asList(arr));
//        } else
//            throw new IllegalArgumentException("Input requires a string array !");

    }
}
