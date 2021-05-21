
package lphy.evolution.sequences;

import jebl.evolution.sequences.State;

import java.util.List;

/**
 * @author Alexei Drummond
 * @author Kylie Chen
 */
public final class PhasedGenotype extends DataType {

    //*** Singleton ***//

    private final String[] stateNames = {"AA", "AC", "AG", "AT","CA", "CC", "CG", "CT", "GA", "GC", "GG", "GT","TA", "TC", "TG", "TT"};


    private static PhasedGenotype instance;
    private PhasedGenotype(){}
    public static PhasedGenotype getInstance(){
        if(instance == null)
            instance = new PhasedGenotype();
        return instance;
    }

    //*** implementations ***//

    public static final String NAME = "phasedGenotypes";

    @Override
    public int getStateCount() {
        return 18;
    }

    @Override
    public List<State> getStates() {
        return null;
    }

    @Override
    public int getCanonicalStateCount() {
        return 16;
    }

    @Override
    public List<? extends State> getCanonicalStates() {
        return null;
    }

    @Override
    public int getCodeLength() {
        return 2;
    }

    @Override
    public State getState(int index) {
        return null;
    }

    @Override
    public State getUnknownState() {
        return null;
    }

    @Override
    public State getGapState() {
        return null;
    }

    @Override
    public boolean isUnknown(State state) {
        return false;
    }

    @Override
    public boolean isGap(State state) {
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getNexusDataType() {
        return NAME;
    }

}
