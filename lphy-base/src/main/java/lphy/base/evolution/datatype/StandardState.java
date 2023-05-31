
package lphy.base.evolution.datatype;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;

/**
 * @author Walter Xie
 */
public final class StandardState extends State {

    /**
     * The state code will be the name.
     * @param name
     * @param index
     */
    public StandardState(String name, int index) {
//        super(name, String.valueOf(index), index);
        super(name, name, index);
    }

    public StandardState(String name, int index, StandardState[] ambiguities) {
//        super(name, String.valueOf(index), index, ambiguities);
        super(name, name, index, ambiguities);
    }

    @Override
    public int compareTo(Object o) {
        // throws ClassCastException on across-class comparison
        StandardState that = (StandardState) o;
        return super.compareTo(that);
    }

    public boolean isGap() {
        throw new UnsupportedOperationException("Require to construct after given the number of states !");
	}

    public SequenceType getType() {
        throw new UnsupportedOperationException("Require to construct after given the number of states !");
    }

}
