package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.Value;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.vectorization.operation.Slice;

public class SliceTimeTreeArray extends Slice<TimeTree> {
    public SliceTimeTreeArray(@ParameterInfo(name = ParameterNames.StartParamName, description = "start index") Value<Integer> start,
                              @ParameterInfo(name = ParameterNames.EndParamName, description = "end index") Value<Integer> end,
                              @ParameterInfo(name = ParameterNames.ArrayParamName, description = "array of time trees to slice") Value<TimeTree[]> array) {

        super(start, end, array);
    }
}
