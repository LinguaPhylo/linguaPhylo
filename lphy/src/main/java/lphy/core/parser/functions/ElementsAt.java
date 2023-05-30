package lphy.core.parser.functions;

import lphy.core.graphicalmodel.components.*;

import java.lang.reflect.Array;

public class ElementsAt<T> extends DeterministicFunction {

    public ElementsAt(@ParameterInfo(name=ParameterNames.IndexParamName, description ="index list") Value<Integer[]> i,
                      @ParameterInfo(name=ParameterNames.ArrayParamName, description ="array to retrieve element of") Value<T[]> array) {

        setParam(ParameterNames.IndexParamName, i);
        setParam(ParameterNames.ArrayParamName, array);
    }

    @Override
    @GeneratorInfo(name = "elementsAt", description = "A function to extract element(s) from an array by index.")
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
        return (Value<T[]>)paramMap.get(ParameterNames.ArrayParamName);
    }

    public Value<Integer[]> index() {
        return (Value<Integer[]>) paramMap.get(ParameterNames.IndexParamName);
    }

    public String codeString() {

        String arrayString = array().codeString();

        if (!array().isAnonymous()) {
            arrayString = array().getId();
        }

        Value<Integer[]> index = index();

        String indexString = null;
        if (index.isAnonymous()) {
            indexString = index.codeString();
        } else {
            indexString = index.getId();
        }

        if (!index().isAnonymous()) {
            return super.codeString();
        }
        return arrayString + "[" + indexString + "]";
    }
}
