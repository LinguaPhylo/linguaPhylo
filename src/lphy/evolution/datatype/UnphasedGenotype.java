package lphy.evolution.datatype;

import jebl.evolution.sequences.State;
import jebl.evolution.sequences.UnphasedGenotypeState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * For unphased genotype data.
 * @author Alexei Drummond
 * @author Kylie Chen
 * @author Walter Xie
 */
public class UnphasedGenotype extends DataType {

    public static final String NAME = "unphasedGenotype";
    public static final int CANONICAL_STATE_COUNT = 10;
    public static final int STATE_COUNT = 12;

    public static final UnphasedGenotypeState[] CANONICAL_STATES;
    public static final UnphasedGenotypeState[] STATES;
    public static final UnphasedGenotypeState UNKNOWN_STATE;
    public static final UnphasedGenotypeState GAP_STATE;

    static {
        CANONICAL_STATES = new UnphasedGenotypeState[CANONICAL_STATE_COUNT];

        int x = 0;
        char code;
        for(int i = 0; i < 4; i++) {
            for(int j = i+1; j < 4; j++) {
                String name = "" + NUCL_CHAR[i] + NUCL_CHAR[j];
                code = (char) (x + '0');
                CANONICAL_STATES[x] = new UnphasedGenotypeState(name, Character.toString(code), x);
                x++;
            }
        }
        assert x == CANONICAL_STATE_COUNT;

        // no ambiguous states

        UNKNOWN_STATE = new UnphasedGenotypeState("unknown genotype", "?", 11, CANONICAL_STATES);
        GAP_STATE = new UnphasedGenotypeState("gap", "-", 12, CANONICAL_STATES);
        STATES = new UnphasedGenotypeState[STATE_COUNT];

        int i;
        for(i = 0; i < CANONICAL_STATE_COUNT; ++i) {
            STATES[i] = CANONICAL_STATES[i];
        }

        STATES[11] = UNKNOWN_STATE;
        STATES[12] = GAP_STATE;

    }

    //*** Singleton ***//

    public static UnphasedGenotype INSTANCE = new UnphasedGenotype();
    private UnphasedGenotype(){}

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
