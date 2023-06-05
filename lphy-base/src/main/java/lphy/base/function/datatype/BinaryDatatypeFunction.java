package lphy.base.function.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.datatype.Binary;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.model.component.Value;

public class BinaryDatatypeFunction extends DeterministicFunction<SequenceType> {

    public BinaryDatatypeFunction() {}

    @GeneratorInfo(name = "binaryDataType", verbClause = "is", narrativeName = "binary data type",
            category = GeneratorCategory.SEQU_TYPE,
            description = "The binary data type.")
    public Value<SequenceType> apply() {
        return new Value<>(null, Binary.getInstance(), this);
    }
}
