package lphy.base.functions;

import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.GeneratorInfo;
import lphy.core.model.components.ParameterInfo;
import lphy.core.model.components.Value;
import lphy.core.model.types.IntegerValue;

public class ParseInt extends DeterministicFunction<Integer> {

    public static final String stringParamName = "str";

    public ParseInt(@ParameterInfo(name = stringParamName,
            description = "the string value to parse into an integer.") Value<String> str) {

        setParam(stringParamName, str);
    }

    @Override
    @GeneratorInfo(name = "parseInt", description = "A function to parse the given string to an integer.")
    public Value<Integer> apply() {

        String str = getString().value();
        try {
            Integer i = Integer.parseInt(str);
            return new IntegerValue(i, this);
        } catch (NumberFormatException e) {
            return new IntegerValue(null, this);
        }
    }

    public Value<String> getString() {
        return (Value<String>) paramMap.get(stringParamName);
    }
}
