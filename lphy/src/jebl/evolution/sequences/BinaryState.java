
package jebl.evolution.sequences;

/**
 * @author Walter Xie
 */
public final class BinaryState extends State {

    BinaryState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    BinaryState(String name, String stateCode, int index, BinaryState[] ambiguities) {
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

    public SequenceType getType() { return DataType.BINARY; }

}
