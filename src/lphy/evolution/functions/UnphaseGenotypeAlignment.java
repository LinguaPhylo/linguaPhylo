package lphy.evolution.functions;

import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.sequences.UnphasedGenotype;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class UnphaseGenotypeAlignment extends DeterministicFunction<Alignment> {

    private static final String alignmentParamName = "alignment";

    public UnphaseGenotypeAlignment(@ParameterInfo(name = alignmentParamName, description = "the genotype alignment.") Value<Alignment> x) {
        setParam(alignmentParamName, x);
    }

    @GeneratorInfo(name = "unphase", description = "Returns the unphased version of the phased genotype alignment.")
    public Value<Alignment> apply() {
        Value<Alignment> v = (Value<Alignment>) getParams().get(alignmentParamName);

        // Do unphasing

        Alignment unphasedAlignment = new SimpleAlignment(v.value().taxa(), v.value().nchar(), UnphasedGenotype.INSTANCE);

        for (int i = 0; i < unphasedAlignment.ntaxa(); i++) {
            for (int j = 0; j < unphasedAlignment.nchar(); j++) {
                unphasedAlignment.setState(i, j, unphase(v.value().getState(i, j)));
            }
        }

        return new Value(null, unphasedAlignment, this);
    }

    private int unphase(int state) {
        switch (state) {
            case 0:
                return 0;  // AA -> AA
            case 1:
                return 1;  // AC -> AC
            case 2:
                return 2;  // AG -> AG
            case 3:
                return 3;  // AT -> AT
            case 4:
                return 1;  // CA -> AC
            case 5:
                return 4;  // CC -> CC
            case 6:
                return 5;  // CG -> CG
            case 7:
                return 6;  // CT -> CT
            case 8:
                return 2;  // GA -> AG
            case 9:
                return 5;  // GC -> CG
            case 10:
                return 7;  // GG -> GG
            case 11:
                return 8;  // GT -> GT
            case 12:
                return 3;  // TA -> AT
            case 13:
                return 6;  // TC -> CT
            case 14:
                return 8;  // TG -> GT
            case 15:
                return 9;  // TT -> TT
            case 16:
                return 10; // ?? -> ??
            case 17:
                return 11; // -- -> --
        }
        throw new RuntimeException("Unexpected state: " + state);
    }
}
