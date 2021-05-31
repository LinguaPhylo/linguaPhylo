
package lphy.evolution.sequences;

import jebl.evolution.sequences.StandardState;
import jebl.evolution.sequences.State;

import java.util.*;

/**
 * Used for discrete/categorical data, e.g. morphology, locations, traits, ...
 * @author Walter Xie
 */
public class Standard extends DataType {

    private final StandardState[] CANONICAL_STATES;
    private final StandardState[] STATES;
    private final StandardState UNKNOWN_STATE;
//    private final StandardState GAP_STATE;

    private final Map<String, StandardState> statesByCode; //TODO

    /**
     * Create states from integers
     * @param numCanoStates The number of canonical states excluding unknown and gaps
     */
    public Standard(int numCanoStates){
        assert numCanoStates > 1;
        List<StandardState> states = new ArrayList<>(numCanoStates);
        for (int i = 0; i < numCanoStates; i++) {
            String name = String.valueOf(i);
            StandardState state = new StandardState(name, i);
            states.add(state);
        }

        CANONICAL_STATES = states.toArray(StandardState[]::new);

        int len = CANONICAL_STATES.length;
        UNKNOWN_STATE = new StandardState("?", len, CANONICAL_STATES);
//        GAP_STATE = new StandardState("-", len+1, CANONICAL_STATES);

        // no gap
        STATES = new StandardState[len+1];
        for(int i = 0; i < len; i++) {
            STATES[i] = CANONICAL_STATES[i];
        }
        STATES[len] = UNKNOWN_STATE;
//        STATES[len+1] = GAP_STATE;

        statesByCode = new HashMap();
        for(int i = 0; i < STATES.length; i++) {
            statesByCode.put(STATES[i].getCode(), STATES[i]);
        }
    }

    /**
     * Create states from given unique names.
     * @param stateNames The names of canonical states excluding unknown and gaps
     */
    public Standard(List<String> stateNames){
        assert stateNames.size() > 1;
        List<StandardState> states = new ArrayList<>(stateNames.size());
        for (int i = 0; i < stateNames.size(); i++) {
            String name = stateNames.get(i);
            StandardState state = new StandardState(name, i);
            states.add(state);
        }

        CANONICAL_STATES = states.toArray(StandardState[]::new);

        int len = CANONICAL_STATES.length;
        UNKNOWN_STATE = new StandardState("?", len, CANONICAL_STATES);
//        GAP_STATE = new StandardState("-", len+1, CANONICAL_STATES);

        // no gap
        STATES = new StandardState[len+1];
        for(int i = 0; i < len; i++) {
            STATES[i] = CANONICAL_STATES[i];
        }
        STATES[len] = UNKNOWN_STATE;
//        STATES[len+1] = GAP_STATE;

        statesByCode = new HashMap();
        for(int i = 0; i < STATES.length; i++) {
            statesByCode.put(STATES[i].getCode(), STATES[i]);
        }
    }

    public StandardState getStateFromName(String name) {
        for (StandardState state : STATES) {
            if (state.getFullName().equals(name))
                return state;
        }
        return null;
    }

    //*** implementations ***//

    public static final String NAME = "standard";

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
    public List<? extends State> getCanonicalStates() {
        return Collections.unmodifiableList(Arrays.asList((State[])CANONICAL_STATES));
    }

    @Override
    public int getCodeLength() {
        throw new UnsupportedOperationException("Standard data type only allow 1 site in the sequence, " +
                "normally used by traits !");
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
        throw new UnsupportedOperationException("");
    }

    @Override
    public boolean isUnknown(State state) {
        return state == UNKNOWN_STATE;
    }

    @Override
    public boolean isGap(State state) {
        throw new UnsupportedOperationException("");
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
