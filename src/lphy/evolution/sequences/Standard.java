
package lphy.evolution.sequences;

import jebl.evolution.sequences.State;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Walter Xie
 */
public class Standard extends DataType {

    private int numStates;

//    public Standard(){    }

    //TODO
    public Standard(int numStates){
        this.numStates = numStates;
    }

    //*** implementations ***//

    public static final String NAME = "standard";

    @Override
    public int getStateCount() {
        return numStates;
    }

    @Override
    public List<State> getStates() {
        return new ArrayList<>();
    }

    @Override
    public int getCanonicalStateCount() {
        return numStates;
    }

    @Override
    public List<? extends State> getCanonicalStates() {
        return null;
    }

    @Override
    public int getCodeLength() {
        return 1;
    }

    @Override
    public State getState(int index) {
        return null;
    }

    @Override
    public State getUnknownState() {
        return null;
    }

    @Override
    public State getGapState() {
        return null;
    }

    @Override
    public boolean isUnknown(State state) {
        return false;
    }

    @Override
    public boolean isGap(State state) {
        return false;
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
