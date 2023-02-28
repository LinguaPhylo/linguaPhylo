package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.util.Objects;

public class Concat2Str extends DeterministicFunction {

    public static final String firstParamName = "prefix";
    public static final String secondParamName = "suffix";

    public Concat2Str(@ParameterInfo(name = firstParamName, description ="the prefix substring to concatenate.")
                  Value<String> substr1,
                      @ParameterInfo(name = secondParamName, description ="the suffix substring to concatenate.")
                  Value<String> substr2) {

        setParam(firstParamName, substr1);
        setParam(secondParamName, substr2);
    }

    @Override
    @GeneratorInfo(name = "concat2Str", description = "A function to concatenate substrings into one sting.")
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
