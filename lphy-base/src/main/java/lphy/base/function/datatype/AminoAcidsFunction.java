package lphy.base.function.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;

/**
 * Note: the {@link jebl.evolution.sequences.AminoAcids} creates 22 amino acids.
 * But the alignment is simulated by Q, so the subst model determines the actual states.
 */
public class AminoAcidsFunction extends DeterministicFunction<SequenceType> {

    public AminoAcidsFunction() {}

    @GeneratorInfo(name = "aminoAcids", verbClause = "is", narrativeName = "amino acid data type",
            category = GeneratorCategory.SEQU_TYPE, examples = {"wagCoalescent.lphy"},
            description = "The amino acid data type.")
    public Value<SequenceType> apply() {
        return new Value<>(null, SequenceType.AMINO_ACID, this);
    }
}
