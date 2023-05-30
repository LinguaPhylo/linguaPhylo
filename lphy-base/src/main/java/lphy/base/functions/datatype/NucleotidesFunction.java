package lphy.base.functions.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.GeneratorCategory;
import lphy.core.graphicalmodel.components.GeneratorInfo;
import lphy.core.graphicalmodel.components.Value;

public class NucleotidesFunction extends DeterministicFunction<SequenceType> {

    public NucleotidesFunction() {}

    @GeneratorInfo(name = "nucleotides", verbClause = "is", narrativeName = "nucleotide data type",
            category = GeneratorCategory.SEQU_TYPE, examples = {"primates2.lphy"},
            description = "The nucleotide data type.")
    public Value<SequenceType> apply() {
        return new Value<>(null, SequenceType.NUCLEOTIDE, this);
    }
}
