package lphy.core.parser.functions;

import lphy.core.model.components.ParameterInfo;
import lphy.core.model.components.Value;

public class SliceDoubleArray extends Slice<Double> {
    public SliceDoubleArray(@ParameterInfo(name = ParameterNames.StartParamName, description = "start index") Value<Integer> start,
                            @ParameterInfo(name = ParameterNames.EndParamName, description = "end index") Value<Integer> end,
                            @ParameterInfo(name = ParameterNames.ArrayParamName, description = "array of doubles to slice") Value<Double[]> array) {

        super(start, end, array);
    }
}
