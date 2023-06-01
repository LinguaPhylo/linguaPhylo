package lphy.base.functions;

import lphy.base.ParameterNames;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.components.ParameterInfo;
import lphy.core.model.components.Value;
import lphy.core.parser.functions.Slice;

public class SliceTimeTreeArray extends Slice<TimeTree> {
    public SliceTimeTreeArray(@ParameterInfo(name = ParameterNames.StartParamName, description = "start index") Value<Integer> start,
                              @ParameterInfo(name = ParameterNames.EndParamName, description = "end index") Value<Integer> end,
                              @ParameterInfo(name = ParameterNames.ArrayParamName, description = "array of time trees to slice") Value<TimeTree[]> array) {

        super(start, end, array);
    }
}
