package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class ElementsAt<T> extends DeterministicFunction<T[]> {

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
    public Value<T[]> apply() {

        Value<T[]> array = array();
        Integer[] index = index().value();

        T[] newArray = (T[]) new Object[index.length];
        for (int i = 0; i < index.length; i++) {
            newArray[i] = array.value()[index[i]];
        }

        return new Value<>(null, newArray, this);
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
            return indexString = index().getId();
        }
        return arrayString + "[" + indexString + "]";
    }
}
