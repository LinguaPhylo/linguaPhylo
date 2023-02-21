package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.CompoundVectorValue;

import java.util.*;

public class Unique extends DeterministicFunction<CompoundVectorValue> {

    public static final String argParamName = "arg";

    public Unique(@ParameterInfo(name = argParamName, verb="of",
            description = "the string array to find its unique set.") Value<?> x) {
        setParam(argParamName, x);
    }

    @GeneratorInfo(name="unique", verbClause = "provides", description = "the unique set of the array")
    public CompoundVectorValue apply() {
        Value<?> v = getParams().get(argParamName);
        Set<Object> uniqObj = new LinkedHashSet<>();
        List<Value<?>> uniqVal = new ArrayList<>();
        if (v instanceof CompoundVectorValue) {
            CompoundVectorValue cvv = (CompoundVectorValue) v;
            for (int i = 0; i < cvv.size(); i++) {
                Value val = cvv.getComponentValue(i);
                Object obj = Objects.requireNonNull(val).value();
                if (!uniqObj.contains(obj)) {
                    uniqObj.add(obj);
                    uniqVal.add(val);
                }
            }
        } else
            throw new UnsupportedOperationException("Input requires an array or vector !");

//        StringArray strArr = Objects.requireNonNull(v).value();
//        Set<String> uniqArr;
//        if (strArr.getClass().isArray()) {
//            String[] arr = strArr.value();
//            uniqArr = new LinkedHashSet<>(Arrays.asList(arr));
//        } else
//            throw new IllegalArgumentException("Input requires a string array !");

        return new CompoundVectorValue(null, uniqVal, this);
    }
}
