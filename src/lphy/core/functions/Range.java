package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class Range extends DeterministicFunction<Integer[]> {

    public static final String startParamName = "start";
    public static final String endParamName = "end";

    public Range(@ParameterInfo(name=startParamName, description ="start of the range (inclusive)") Value<Integer> start,
                 @ParameterInfo(name=endParamName, description ="end of the range (inclusive)") Value<Integer> end) {

        setParam(endParamName, end);
        setParam(startParamName, start);
    }

    @Override
    public Value<Integer[]> apply() {

        int s = start().value();
        int e = end().value();
        Integer[] range = new Integer[e-s+1];
        for (int i = s; i <= e; i++) {
            range[i-s] = i;
        }
        return new Value<>(null, range, this);
    }

    public Value<Integer> start() {
        return (Value<Integer>)paramMap.get(startParamName);
    }

    public Value<Integer> end() {
        return (Value<Integer>) paramMap.get(endParamName);
    }

    public String codeString() {
        return start().codeString() + ":" + end().codeString();
    }
}
