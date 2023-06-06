package lphy.base.function.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.GeneratorCategory;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;

public class NucleotidesFunction extends DeterministicFunction<SequenceType> {

    public NucleotidesFunction() {}

    @GeneratorInfo(name = "nucleotides", verbClause = "is", narrativeName = "nucleotide data type",
            category = GeneratorCategory.SEQU_TYPE, examples = {"primates2.lphy"},
            description = "The nucleotide data type.")
    public Value<SequenceType> apply() {
        return new Value<>(null, SequenceType.NUCLEOTIDE, this);
    }
}
