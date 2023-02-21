package lphy.core.functions;

import lphy.graphicalModel.*;

import static lphy.core.ParameterNames.EndParamName;
import static lphy.core.ParameterNames.StartParamName;

public class Range extends DeterministicFunction<Integer[]> implements RangeElement {

    public Range(@ParameterInfo(name=StartParamName, description ="start of the range (inclusive)") Value<Integer> start,
                 @ParameterInfo(name=EndParamName, description ="end of the range (inclusive)") Value<Integer> end) {

        setParam(EndParamName, end);
        setParam(StartParamName, start);
    }

    @Override
    @GeneratorInfo(name = "rangeInt", description = "The range of integers from start to end. Boundaries are included.")
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
        return (Value<Integer>)paramMap.get(StartParamName);
    }

    public Value<Integer> end() {
        return (Value<Integer>) paramMap.get(EndParamName);
    }

    @Override
    public Integer[] range() {
        return value();
    }

    public String codeString() {
        return start().codeString() + ":" + end().codeString();
    }
}
