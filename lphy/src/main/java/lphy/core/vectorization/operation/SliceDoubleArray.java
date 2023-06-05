package lphy.core.vectorization.operation;

import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.Value;

public class SliceDoubleArray extends Slice<Double> {
    public SliceDoubleArray(@ParameterInfo(name = ParameterNames.StartParamName, description = "start index") Value<Integer> start,
                            @ParameterInfo(name = ParameterNames.EndParamName, description = "end index") Value<Integer> end,
                            @ParameterInfo(name = ParameterNames.ArrayParamName, description = "array of doubles to slice") Value<Double[]> array) {

        super(start, end, array);
    }
}
