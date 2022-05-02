package lphy.core.functions.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.datatype.Binary;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorCategory;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.Value;

public class BinaryDatatypeFunction extends DeterministicFunction<SequenceType> {

    public BinaryDatatypeFunction() {}

    @GeneratorInfo(name = "binaryDataType", verbClause = "is", narrativeName = "binary data type",
            category = GeneratorCategory.SEQU_TYPE,
            description = "The binary data type.")
    public Value<SequenceType> apply() {
        return new Value<>(null, Binary.getInstance(), this);
    }
}
