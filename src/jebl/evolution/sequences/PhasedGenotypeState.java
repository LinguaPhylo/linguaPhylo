package jebl.evolution.sequences;

import lphy.evolution.datatype.PhasedGenotype;

public class PhasedGenotypeState extends State {


    public PhasedGenotypeState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    public PhasedGenotypeState(String name, String stateCode, int index, State[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }

    @Override
    public boolean isGap() {
        return this == PhasedGenotype.GAP_STATE;
    }

    @Override
    public SequenceType getType() {
        return PhasedGenotype.INSTANCE;
    }
}
