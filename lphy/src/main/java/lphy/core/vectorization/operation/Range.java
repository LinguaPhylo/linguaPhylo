package lphy.core.vectorization.operation;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.RangeElement;

public class Range extends DeterministicFunction<Integer[]> implements RangeElement {

    public Range(@ParameterInfo(name= ParameterNames.StartParamName, description ="start of the range (inclusive)") Value<Integer> start,
                 @ParameterInfo(name= ParameterNames.EndParamName, description ="end of the range (inclusive)") Value<Integer> end) {

        setParam(ParameterNames.EndParamName, end);
        setParam(ParameterNames.StartParamName, start);
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
        return (Value<Integer>)paramMap.get(ParameterNames.StartParamName);
    }

    public Value<Integer> end() {
        return (Value<Integer>) paramMap.get(ParameterNames.EndParamName);
    }

    @Override
    public Integer[] range() {
        return value();
    }

    public String codeString() {
        // issue 421 : taxa(names=1:n) is incorrectly rendered to  taxa(names=1:n = 10)
        String str;
        Value<Integer> start = start();
        if (start.isAnonymous())
            str = start.codeString();
        else str = start.getId();
        str += ":";
        Value<Integer> end = end();
        if (end.isAnonymous())
            str += end.codeString();
        else str += end.getId();
        return str;
    }
}
