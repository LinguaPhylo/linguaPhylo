package lphy.base.functions.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.datatype.Binary;
import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.GeneratorCategory;
import lphy.core.graphicalmodel.components.GeneratorInfo;
import lphy.core.graphicalmodel.components.Value;

public class BinaryDatatypeFunction extends DeterministicFunction<SequenceType> {

    public BinaryDatatypeFunction() {}

    @GeneratorInfo(name = "binaryDataType", verbClause = "is", narrativeName = "binary data type",
            category = GeneratorCategory.SEQU_TYPE,
            description = "The binary data type.")
    public Value<SequenceType> apply() {
        return new Value<>(null, Binary.getInstance(), this);
    }
}
