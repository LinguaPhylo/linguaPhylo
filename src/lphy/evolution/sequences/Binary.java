
package lphy.evolution.sequences;

import jebl.evolution.sequences.State;

import java.util.List;

/**
 * @author Walter Xie
 */
public final class Binary extends DataType {

    //*** Singleton ***//

    private static Binary instance;
    private Binary(){}
    public static Binary getInstance(){
        if(instance == null)
            instance = new Binary();
        return instance;
    }

    //*** implementations ***//

    public static final String NAME = "binary";

//    public static final State ZERO_STATE = new State("0", "0", 0);
//    public static final State ONE_STATE = new State("1", "1", 1);
//
//    public static final State[] CANONICAL_STATES = new State[] {
//            ZERO_STATE, ONE_STATE
//    };
//
//    public static final State UNKNOWN_STATE = new State("Unknown base", "?", 2, CANONICAL_STATES);
//    public static final State GAP_STATE = new State("Gap", "-", 3, CANONICAL_STATES);
//
//    public static final State[] STATES = new State[] {
//            ZERO_STATE, ONE_STATE, UNKNOWN_STATE, GAP_STATE
//    };


    @Override
    public int getStateCount() {
        return 4;
    }

    @Override
    public List<State> getStates() {
        return null;
    }

    @Override
    public int getCanonicalStateCount() {
        return 2;
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
