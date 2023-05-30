package lphy.core.parser.functions;

import lphy.core.graphicalmodel.components.*;

import java.lang.reflect.Array;

public class Slice<T> extends DeterministicFunction {

    public Slice(@ParameterInfo(name=ParameterNames.StartParamName, description ="start index") Value<Integer> start,
                 @ParameterInfo(name=ParameterNames.EndParamName, description ="end index") Value<Integer> end,
                 @ParameterInfo(name=ParameterNames.ArrayParamName, description ="array to retrieve element of") Value<T[]> array) {

        setInput(ParameterNames.StartParamName, start);
        setInput(ParameterNames.EndParamName, end);
        setInput(ParameterNames.ArrayParamName, array);
    }

    @Override
    @GeneratorInfo(name = "slice", description = "A function to slice a subarray from an array.")
    public Value apply() {

        Value<T[]> array = array();
        Integer start = start().value();
        Integer end = end().value();


        if (end > start) {
            T[] newArray = (T[])Array.newInstance(array.value().getClass().getComponentType(), end-start+1);
            for (int i = start; i <= end; i++) {
                newArray[i] = array.value()[i];
            }
            return ValueUtils.createValue(newArray, this);
        } else {
            return ValueUtils.createValue(array.value()[start], this);
        }
    }

    public Value<T[]> array() {
        return (Value<T[]>)paramMap.get(ParameterNames.ArrayParamName);
    }

    public Value<Integer> start() {
        return (Value<Integer>) paramMap.get(ParameterNames.StartParamName);
    }

    public Value<Integer> end() {
        return (Value<Integer>) paramMap.get(ParameterNames.EndParamName);
    }

    public String codeString() {

        String arrayString = array().codeString();

        if (!array().isAnonymous()) {
            arrayString = array().getId();
        }

        String startString = start().codeString();
        String endString = end().codeString();

        if (!start().isAnonymous() || !end().isAnonymous()) {
            return super.codeString();
        }

        if (startString.equals(endString)) {
            return arrayString + "[" + startString + "]";
        }
        return arrayString + "[" + startString  + ":" + endString + "]";
    }

    public int size() {
        return end().value() - start().value() + 1;
    }
}
