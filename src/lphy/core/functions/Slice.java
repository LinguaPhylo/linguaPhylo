package lphy.core.functions;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.*;

import java.lang.reflect.Array;

public class Slice<T> extends DeterministicFunction {

    public static final String startParamName = "start";
    public static final String endParamName = "end";
    public static final String arrayParamName = "array";

    public Slice(@ParameterInfo(name=startParamName, description ="start index") Value<Integer> start,
                 @ParameterInfo(name=endParamName, description ="end index") Value<Integer> end,
                 @ParameterInfo(name=arrayParamName, description ="array to retrieve element of") Value<T[]> array) {

        setInput(startParamName, start);
        setInput(endParamName, end);
        setInput(arrayParamName, array);
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
        return (Value<T[]>)paramMap.get(arrayParamName);
    }

    public Value<Integer> start() {
        return (Value<Integer>) paramMap.get(startParamName);
    }

    public Value<Integer> end() {
        return (Value<Integer>) paramMap.get(endParamName);
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

}
