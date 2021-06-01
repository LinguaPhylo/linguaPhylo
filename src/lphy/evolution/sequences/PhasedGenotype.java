
package lphy.evolution.sequences;

import jebl.evolution.sequences.PhasedGenotypeState;
import jebl.evolution.sequences.State;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * For phased genotype data.
 * @author Alexei Drummond
 * @author Kylie Chen
 * @author Walter Xie
 */
public final class PhasedGenotype extends DataType {

    public static final String NAME = "phasedGenotype";
    public static final int CANONICAL_STATE_COUNT = 16;
    public static final int STATE_COUNT = 24;

    public static final PhasedGenotypeState[] CANONICAL_STATES;
    public static final PhasedGenotypeState[] STATES;
    public static final PhasedGenotypeState UNKNOWN_STATE;
    public static final PhasedGenotypeState GAP_STATE;
    public static final PhasedGenotypeState AC_OR_CA;
    public static final PhasedGenotypeState AG_OR_GA;
    public static final PhasedGenotypeState AT_OR_TA;
    public static final PhasedGenotypeState CG_OR_GC;
    public static final PhasedGenotypeState CT_OR_TC;
    public static final PhasedGenotypeState GT_OR_TG;

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

        AC_OR_CA = new PhasedGenotypeState("ac", "M", 16, CANONICAL_STATES);
        AG_OR_GA = new PhasedGenotypeState("ag", "R", 17, CANONICAL_STATES);
        AT_OR_TA = new PhasedGenotypeState("at", "W", 18, CANONICAL_STATES);
        CG_OR_GC = new PhasedGenotypeState("cg", "S", 19, CANONICAL_STATES);
        CT_OR_TC = new PhasedGenotypeState("ct", "Y", 20, CANONICAL_STATES);
        GT_OR_TG = new PhasedGenotypeState("gt", "K", 21, CANONICAL_STATES);
        UNKNOWN_STATE = new PhasedGenotypeState("??", "?", 22, CANONICAL_STATES);
        GAP_STATE = new PhasedGenotypeState("--", "-", 23, CANONICAL_STATES);
        STATES = new PhasedGenotypeState[STATE_COUNT];

        int i;
        for(i = 0; i < CANONICAL_STATE_COUNT; ++i) {
            STATES[i] = CANONICAL_STATES[i];
        }

        STATES[16] = AC_OR_CA;
        STATES[17] = AG_OR_GA;
        STATES[18] = AT_OR_TA;
        STATES[19] = CG_OR_GC;
        STATES[20] = CT_OR_TC;
        STATES[21] = GT_OR_TG;
        STATES[22] = UNKNOWN_STATE;
        STATES[23] = GAP_STATE;
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
