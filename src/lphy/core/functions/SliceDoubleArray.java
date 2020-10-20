package lphy.core.functions;

import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class SliceDoubleArray extends Slice<Double> {
    public SliceDoubleArray(@ParameterInfo(name = startParamName, description = "start index") Value<Integer> start,
                            @ParameterInfo(name = endParamName, description = "end index") Value<Integer> end,
                            @ParameterInfo(name = arrayParamName, description = "array of doubles to slice") Value<Double[]> array) {

        super(start, end, array);
    }
}
