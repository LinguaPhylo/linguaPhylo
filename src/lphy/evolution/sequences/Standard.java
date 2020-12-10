
package lphy.evolution.sequences;

import jebl.evolution.sequences.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Walter Xie
 */
public class Standard extends DataType {

//    final private int numStates;
    private final List<String> stateNames;

    public Standard(int numStates){
        stateNames = createStateNames(numStates);
    }

    //Cannot use jebl State outside of jebl
    public Standard(List<String> stateNames){
        this.stateNames = new ArrayList<>(Objects.requireNonNull(stateNames));
    }

    private List<String> createStateNames(int numStates) {
        assert numStates > 1;
        return IntStream.range(0, numStates).mapToObj(String::valueOf).collect(Collectors.toList());
    }


    public List<String> getStateNames() {
        return Objects.requireNonNull(stateNames);
    }

    public String getStateName(int index) {
        return Objects.requireNonNull(stateNames).get(index);
    }

    public int getStateNameIndex(String name) {
        return Objects.requireNonNull(stateNames).indexOf(name);
    }

    //*** implementations ***//

    public static final String NAME = "standard";

    @Override
    public int getStateCount() {
        return stateNames.size(); // TODO ambiguous?
    }

    @Override
    public List<State> getStates() {
        throw new UnsupportedOperationException("jebl State is package-private, cannot be inherited !");
    }

    @Override
    public int getCanonicalStateCount() {
        return stateNames.size();
    }

    @Override
    public List<? extends State> getCanonicalStates() {
        throw new UnsupportedOperationException("jebl State is package-private, cannot be inherited !");
    }

    @Override
    public int getCodeLength() {
        return 1;
    }

    @Override
    public State getState(int index) {
        throw new UnsupportedOperationException("jebl State is package-private, cannot be inherited !");
    }

    @Override
    public State getUnknownState() {
        throw new UnsupportedOperationException("jebl State is package-private, cannot be inherited !");
    }

    @Override
    public State getGapState() {
        throw new UnsupportedOperationException("jebl State is package-private, cannot be inherited !");
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
