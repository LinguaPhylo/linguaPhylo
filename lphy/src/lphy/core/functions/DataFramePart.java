package lphy.core.functions;

import lphy.evolution.DataFrame;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class DataFramePart extends DeterministicFunction<DataFrame> {

    String frameParamName;
    String nameParamName;

    public DataFramePart(@ParameterInfo(name = "frame", description = "the frame to extract the part from.") Value<DataFrame> frame,
                         @ParameterInfo(name = "name", description = "the name of the part.") Value<String> name) {
        frameParamName = getParamName(0);
        nameParamName = getParamName(1);
        setParam(frameParamName, frame);
        setParam(nameParamName, name);
    }

    @GeneratorInfo(name = "part", description = "Extracts a part of a data frame.")
    public Value<DataFrame> apply() {
        Value<DataFrame> frame = getParams().get(frameParamName);
        Value<String> name = getParams().get(nameParamName);

        return new Value<>(null, frame.value().part(name.value()), this);
    }
}
