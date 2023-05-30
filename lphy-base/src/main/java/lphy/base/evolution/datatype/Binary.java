
package lphy.base.evolution.datatype;

import jebl.evolution.sequences.State;

import java.util.Arrays;
import java.util.Collections;
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

    public static final BinaryState ZERO_STATE = new BinaryState("0", "0", 0);
    public static final BinaryState ONE_STATE = new BinaryState("1", "1", 1);

    public static final BinaryState[] CANONICAL_STATES = new BinaryState[] {
            ZERO_STATE, ONE_STATE
    };

    public static final BinaryState UNKNOWN_STATE = new BinaryState("Unknown base", "?", 2, CANONICAL_STATES);
    public static final BinaryState GAP_STATE = new BinaryState("Gap", "-", 3, CANONICAL_STATES);

    public static final BinaryState[] STATES = new BinaryState[] {
            ZERO_STATE, ONE_STATE, UNKNOWN_STATE, GAP_STATE
    };


    @Override
    public int getStateCount() {
        return STATES.length;
    }

    @Override
    public List<State> getStates() {
        return Collections.unmodifiableList(Arrays.asList((State[])STATES));
    }

    @Override
    public int getCanonicalStateCount() {
        return CANONICAL_STATES.length;
    }

    @Override
    public List<State> getCanonicalStates() {
        return Collections.unmodifiableList(Arrays.asList((State[])CANONICAL_STATES));
    }

    @Override
    public int getCodeLength() {
        return 1;
    }

    @Override
    public State getState(int index) {
        return STATES[index];
    }

    @Override
    public State getUnknownState() {
        return UNKNOWN_STATE;
    }

    @Override
    public State getGapState() {
        return GAP_STATE;
    }

    @Override
    public boolean isUnknown(State state) {
        return state == UNKNOWN_STATE;
    }

    @Override
    public boolean isGap(State state) {
        return state == GAP_STATE;
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
