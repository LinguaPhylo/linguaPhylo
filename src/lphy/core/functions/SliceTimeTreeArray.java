package lphy.core.functions;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class SliceTimeTreeArray extends Slice<TimeTree> {
    public SliceTimeTreeArray(@ParameterInfo(name = startParamName, description = "start index") Value<Integer> start,
                              @ParameterInfo(name = endParamName, description = "end index") Value<Integer> end,
                              @ParameterInfo(name = arrayParamName, description = "array of time trees to slice") Value<TimeTree[]> array) {

        super(start, end, array);
    }
}
