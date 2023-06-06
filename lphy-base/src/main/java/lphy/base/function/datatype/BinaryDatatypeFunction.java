package lphy.base.function.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.datatype.Binary;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;

public class BinaryDatatypeFunction extends DeterministicFunction<SequenceType> {

    public BinaryDatatypeFunction() {}

    @GeneratorInfo(name = "binaryDataType", verbClause = "is", narrativeName = "binary data type",
            category = GeneratorCategory.SEQU_TYPE,
            description = "The binary data type.")
    public Value<SequenceType> apply() {
        return new Value<>(null, Binary.getInstance(), this);
    }
}
