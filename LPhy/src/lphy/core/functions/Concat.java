package lphy.core.functions;

import lphy.graphicalModel.*;

import java.lang.reflect.Array;
import java.util.Objects;

public class Concat extends DeterministicFunction {

    public static final String firstParamName = "prefix";
    public static final String secondParamName = "suffix";

    public Concat(@ParameterInfo(name = firstParamName, description ="the prefix substring to concatenate.")
                         Value<String> substr1,
                  @ParameterInfo(name = secondParamName, description ="the suffix substring to concatenate.")
                         Value<String> substr2) {

        setInput(firstParamName, substr1);
        setInput(secondParamName, substr2);
    }

    @Override
    @GeneratorInfo(name = "concat", description = "A function to concatenate substrings into one sting.")
    public Value<String> apply() {
        Value<String> substr1 = getParams().get(firstParamName);
        Value<String> substr2 = getParams().get(secondParamName);

        return join(substr1,substr2);
    }

    // concatenate Value<String>[] into Value<String>
    private Value<String> join(Value<String>... substr) {
        StringBuilder oneStr = new StringBuilder();
        for (Value<String> s : substr)
            oneStr.append(Objects.requireNonNull(s).value());
        return new Value<String>(oneStr.toString(), this);
    }
}
