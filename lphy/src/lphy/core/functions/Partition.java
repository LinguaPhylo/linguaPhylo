package lphy.core.functions;

import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class Partition extends DeterministicFunction<Alignment> {

    String alignmentParamName;
    String nameParamName;

    public Partition(@ParameterInfo(name = "alignment", description = "the multi-partition alignment to extract the partition from.") Value<CharSetAlignment> alignment,
                     @ParameterInfo(name = "name", description = "the name of the part.") Value<String> name) {
        alignmentParamName = getParamName(0);
        nameParamName = getParamName(1);
        setParam(alignmentParamName, alignment);
        setParam(nameParamName, name);
    }

    @GeneratorInfo(name = "partition", description = "Extracts the partition alignment from a multi-partition alignment.")
    public Value<Alignment> apply() {
        Value<CharSetAlignment> charSetAlignment = getParams().get(alignmentParamName);
        Value<String> name = getParams().get(nameParamName);

        Alignment alignment = (Alignment) charSetAlignment.value().part(name.value());

        return new Value<Alignment>(null, alignment, this);
    }
}
