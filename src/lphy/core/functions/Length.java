package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;

import java.lang.reflect.Array;

public class Length extends DeterministicFunction<Integer> {

    // by setting the arg param to an integer it will not be displayed in the code string.
    public static final String argParamName = "0";

    public Length(@ParameterInfo(name = argParamName, verb="of", suppressNameInNarrative=true, description = "the array to return the length of.") Value x) {
        setParam(argParamName, x);
    }

    @GeneratorInfo(name="length", verbClause = "is", description = "the length of the argument")
    public Value<Integer> apply() {
        Value<?> v = (Value<?>)getParams().get(argParamName);

        Integer length = 1;
        if (v.value().getClass().isArray()) {
            length = Array.getLength(v.value());
        }

        return new IntegerValue(length, this);
    }
}
