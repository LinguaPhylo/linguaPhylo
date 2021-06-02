
package jebl.evolution.sequences;

import lphy.evolution.datatype.Binary;

/**
 * @author Walter Xie
 */
public final class BinaryState extends State {

    public BinaryState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    public BinaryState(String name, String stateCode, int index, BinaryState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }

    @Override
    public int compareTo(Object o) {
        // throws ClassCastException on across-class comparison
        BinaryState that = (BinaryState) o;
        return super.compareTo(that);
    }

    public boolean isGap() {
		return this == Binary.GAP_STATE;
	}

    public SequenceType getType() { return Binary.getInstance(); }

}
