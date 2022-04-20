package lphy.core.functions.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.Value;

/**
 * Note: the {@link jebl.evolution.sequences.AminoAcids} creates 22 amino acids.
 * But the alignment is simulated by Q, so the subst model determines the actual states.
 */
public class AminoAcidsFunction extends DeterministicFunction<SequenceType> {

    public AminoAcidsFunction() {}

    @GeneratorInfo(name = "aminoAcids",
            verbClause = "is",
            narrativeName = "amino acid data type",
            description = "The amino acid data type.")
    public Value<SequenceType> apply() {
        return new Value<>(null, SequenceType.AMINO_ACID, this);
    }
}
