
package lphy.evolution.sequences;

import jebl.evolution.sequences.PhasedGenotypeState;
import jebl.evolution.sequences.State;

import java.util.*;

/**
 * @author Alexei Drummond
 * @author Kylie Chen
 * @author Walter Xie
 */
public final class PhasedGenotype extends DataType {

    public static final String NAME = "phasedGenotype";
    public static final int CANONICAL_STATE_COUNT = 16;
    public static final int STATE_COUNT = 18;

    public static final PhasedGenotypeState[] CANONICAL_STATES;
    public static final PhasedGenotypeState[] STATES;
    public static final PhasedGenotypeState UNKNOWN_STATE;
    public static final PhasedGenotypeState GAP_STATE;
    private static final Map<String, PhasedGenotypeState> statesByCode;

    static {
        CANONICAL_STATES = new PhasedGenotypeState[CANONICAL_STATE_COUNT];

        int x = 0;
        char code;
        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {
                    String name = "" + NUCL_CHAR[i] + NUCL_CHAR[j];
                    code = x < 10 ? (char) (x + '0') : (char) (x - 10 + 'a');
                    CANONICAL_STATES[x] = new PhasedGenotypeState(name, Character.toString(code), x);
                    ++x;
            }
        }
        assert x == CANONICAL_STATE_COUNT;

        // TODO Alexei please check these two
        UNKNOWN_STATE = new PhasedGenotypeState("??", "?", 16, CANONICAL_STATES);
        GAP_STATE = new PhasedGenotypeState("--", "-", 17, CANONICAL_STATES);
        STATES = new PhasedGenotypeState[STATE_COUNT];

        int i;
        for(i = 0; i < CANONICAL_STATE_COUNT; ++i) {
            STATES[i] = CANONICAL_STATES[i];
        }

        STATES[16] = UNKNOWN_STATE;
        STATES[17] = GAP_STATE;
        statesByCode = new HashMap();

        for(i = 0; i < STATES.length; ++i) {
            statesByCode.put(STATES[i].getCode(), STATES[i]);
        }

    }

    //*** Singleton ***//

    public static PhasedGenotype INSTANCE = new PhasedGenotype();
    private PhasedGenotype(){}

    //*** implementations ***//

    @Override
    public int getStateCount() {
        return STATE_COUNT;
    }

    @Override
    public List<State> getStates() {
        return Collections.unmodifiableList(Arrays.asList((State[])STATES));
    }

    @Override
    public int getCanonicalStateCount() {
        return CANONICAL_STATE_COUNT;
    }

    @Override
    public List<? extends State> getCanonicalStates() {
        return Collections.unmodifiableList(Arrays.asList((State[])CANONICAL_STATES));
    }

    @Override
    public int getCodeLength() {
        return 2;
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
