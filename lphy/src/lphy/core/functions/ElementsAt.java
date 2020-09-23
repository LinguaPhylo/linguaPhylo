package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.utils.LoggerUtils;

import java.lang.reflect.Array;

public class ElementsAt<T> extends DeterministicFunction {

    String indexParamName;
    String arrayParamName;

    public ElementsAt(@ParameterInfo(name="index", description ="index list") Value<Integer[]> i,
                      @ParameterInfo(name="array", description ="array to retrieve element of") Value<T[]> array) {

        indexParamName = getParamName(0);
        arrayParamName = getParamName(1);
        setParam(indexParamName, i);
        setParam(arrayParamName, array);
    }

    @Override
    @GeneratorInfo(name = "elementsAt", description = "A function to extract some element from an array by index.")
    public Value apply() {

        Value<T[]> array = array();
        Integer[] index = index().value();

        if (index.length > 1) {
            T[] newArray = (T[])Array.newInstance(array.value().getClass().getComponentType(), index.length);
            for (int i = 0; i < index.length; i++) {
                newArray[i] = array.value()[index[i]];
            }
            return ValueUtils.createValue(newArray, this);
        } else {
            return ValueUtils.createValue(array.value()[index[0]], this);
        }
    }

    public Value<T[]> array() {
        return (Value<T[]>)paramMap.get(arrayParamName);
    }

    public Value<Integer[]> index() {
        return (Value<Integer[]>) paramMap.get(indexParamName);
    }

    public String codeString() {

        String arrayString = array().codeString();

        if (!array().isAnonymous()) {
            arrayString = array().getId();
        }

        String indexString = index().codeString();

        if (!index().isAnonymous()) {
            return super.codeString();
        }
        return arrayString + "[" + indexString + "]";
    }
}
