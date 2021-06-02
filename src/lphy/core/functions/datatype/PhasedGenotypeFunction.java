package lphy.core.functions.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.datatype.PhasedGenotype;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.Value;

public class PhasedGenotypeFunction extends DeterministicFunction<SequenceType> {

    public PhasedGenotypeFunction() {}

    @GeneratorInfo(name = "phasedGenotype",
            verbClause = "is",
            narrativeName = "phased genotype data type",
            description = "The phased genotype data type.")
    public Value<SequenceType> apply() {
        return new Value<>(null, PhasedGenotype.INSTANCE, this);
    }
}
