package lphy.base.evolution.datatype;


import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;

import java.util.Arrays;
import java.util.List;

/**
 * Define the subclasses of {@link SequenceType},
 * implement parsing char/string to integers.
 * The supplement of data types not available from {@link SequenceType}.
 * @author Walter Xie
 */
public abstract class DataType implements SequenceType {

    protected static final char[] NUCL_CHAR = new char[]{'A', 'C', 'G', 'T'};

    // TODO SequenceType implements Comparable

    public static boolean isSame(SequenceType type1, SequenceType type2) {
        if (type1 == null || type2 == null) return false;
        return type1.getName().equalsIgnoreCase(type2.getName());
    }

//    public static boolean isType(Alignment alignment, SequenceType sequenceType) {
//        if (alignment.getSequenceType() == null)
//            throw new IllegalArgumentException("Please define SequenceType !");
////            return alignment.getNumOfStates() == sequenceType.getCanonicalStateCount();
//        return isSame(alignment.getSequenceType(), sequenceType);
//    }

    //*** these should be inherited to reduce duplicated code ***//

    protected static final int STATES_BY_CODE_SIZE = 128;

    /**
     * @return A list of all possible states, including the gap and ambiguity states.
     */
    @Override
    public abstract List<State> getStates();

    @Override
    public State getState(char code) {
        if (code < 0 || code >= STATES_BY_CODE_SIZE)
            return null;
        State[] statesByCode = getStatesByCode();
        return statesByCode[code];
    }

    @Override
    public State getState(String code) {
        return getState(code.charAt(0));
    }

    protected State[] getStatesByCode() {
        State[] statesByCode = new State[STATES_BY_CODE_SIZE];
        // Undefined characters are mapped to null
        Arrays.fill(statesByCode, null);

        for (State state : getStates()) {
            final char code = state.getCode().charAt(0);
            statesByCode[code] = state;
            statesByCode[Character.toLowerCase(code)] = state;
        }
        return statesByCode;
    }


    // TODO review: may not need

    @Override
    public State[] toStateArray(String sequenceString) {
        State[] seq = new State[sequenceString.length()];
        for (int i = 0; i < seq.length; i++) {
            seq[i] = getState(sequenceString.charAt(i));
        }
        return seq;
    }

    @Override
    public State[] toStateArray(byte[] indexArray) {
        State[] seq = new State[indexArray.length];
        for (int i = 0; i < seq.length; i++) {
            seq[i] = getState(indexArray[i]);
        }
        return seq;
    }


} // end of DataType
