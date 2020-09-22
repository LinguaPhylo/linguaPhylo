package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class ElementAt<T> extends DeterministicFunction<T> {

    String indexParamName;
    String arrayParamName;

    public ElementAt(@ParameterInfo(name="index", description ="index range") Value<Integer> i,
                     @ParameterInfo(name="array", description ="array to retrieve element of") Value<T[]> array) {

        indexParamName = getParamName(0);
        arrayParamName = getParamName(1);
        setParam(indexParamName, i);
        setParam(arrayParamName, array);
    }

    @Override
    @GeneratorInfo(name = "elementAt", description = "A function to extract an element from an array by index.")
    public Value<T> apply() {

        Value<T[]> array = array();
        int i = index().value();

        return new Value<>(null, array.value()[i], this);
    }

    public Value<T[]> array() {
        return (Value<T[]>)paramMap.get(arrayParamName);
    }

    public Value<Integer> index() {
        return (Value<Integer>) paramMap.get(indexParamName);
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
