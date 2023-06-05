package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.Value;
import lphy.core.model.datatype.IntegerValue;

import java.lang.reflect.Array;

public class Length extends DeterministicFunction<Integer> {

    // by setting the arg param to an integer it will not be displayed in the code string.
    public static final String argParamName = ParameterNames.NoParamName0;

    public Length(@ParameterInfo(name = ParameterNames.NoParamName0, verb="of",
            suppressNameInNarrative=true, description = "the array to return the length of.") Value x) {
        // this adds value to output, so no arg name works when click sample button
        setInput(argParamName, x);
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
