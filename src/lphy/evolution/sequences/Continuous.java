package lphy.evolution.sequences;

import jebl.evolution.sequences.State;

import java.util.List;

/**
 * @author Walter Xie
 */
public class Continuous extends DataType {

    //*** Singleton ***//
    private static Continuous instance;
    private Continuous(){}
    public static Continuous getInstance(){
        if(instance == null)
            instance = new Continuous();
        return instance;
    }

    //*** implementations ***//

    public static final String NAME = "continuous";

    @Override
    public int getStateCount() {
        return 0;
    }

    @Override
    public List<State> getStates() {
        return null;
    }

    @Override
    public int getCanonicalStateCount() {
        return 0;
    }

    @Override
    public List<? extends State> getCanonicalStates() {
        return null;
    }

    @Override
    public int getCodeLength() {
        return 1;
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
