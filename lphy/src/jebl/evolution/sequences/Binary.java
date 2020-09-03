
package jebl.evolution.sequences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Binary data
 * @author Walter Xie
 */
public final class Binary {

	private Binary() { } // make class uninstantiable

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

	private static final int STATES_BY_CODE_SIZE = 128;

	/**
	 * 4, including gap and ambiguity states
	 */
	public static int getStateCount() { return STATES.length; } // 4

	/**
	 * @return A list of all possible states, including the gap and ambiguity states.
	 */
	public static List<State> getStates() { return Collections.unmodifiableList(Arrays.asList((State[])STATES)); }

	/**
	 * 2, excluding gap and ambiguity states
	 */
	public static int getCanonicalStateCount() { return CANONICAL_STATES.length; }

	public static List<BinaryState> getCanonicalStates() { return Collections.unmodifiableList(Arrays.asList(CANONICAL_STATES)); }

	public static BinaryState getState(char code) {
		if (code < 0 || code >= STATES_BY_CODE_SIZE) {
			return null;
		}
		return statesByCode[code];
	}

	public static BinaryState getState(String code) {
		return getState(code.charAt(0));
	}

	public static BinaryState getState(int index) {
		return STATES[index];
	}

	public static BinaryState getUnknownState() { return UNKNOWN_STATE; }

	public static BinaryState getGapState() { return GAP_STATE; }

	public static boolean isUnknown(State state) { return state == UNKNOWN_STATE; }

	public static boolean isGap(State state) { return state == GAP_STATE; }

	private static final BinaryState[] statesByCode;
	static {
		statesByCode = new BinaryState[STATES_BY_CODE_SIZE];
		for (int i = 0; i < statesByCode.length; i++) {
			// Undefined characters are mapped to null
			statesByCode[i] = null;
		}

		for (BinaryState state : STATES) {
			final char code = state.getCode().charAt(0);
			statesByCode[code] = state;
			statesByCode[Character.toLowerCase(code)] = state;
		}
	}

	public static BinaryState[] toStateArray(String sequenceString) {
		BinaryState[] seq = new BinaryState[sequenceString.length()];
		for (int i = 0; i < seq.length; i++) {
			seq[i] = getState(sequenceString.charAt(i));
		}
		return seq;
	}

	public static BinaryState[] toStateArray(byte[] indexArray) {
		BinaryState[] seq = new BinaryState[indexArray.length];
		for (int i = 0; i < seq.length; i++) {
			seq[i] = getState(indexArray[i]);
		}
		return seq;
	}


}
