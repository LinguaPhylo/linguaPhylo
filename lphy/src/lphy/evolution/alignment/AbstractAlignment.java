package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.app.AlignmentComponent;
import lphy.app.HasComponentView;
import lphy.evolution.DataFrame;
import lphy.evolution.Taxa;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Everything related to Taxa, Data type
 * @author Walter Xie
 */
public abstract class AbstractAlignment extends DataFrame implements Taxa, HasComponentView<AbstractAlignment> {

    Map<String, Integer> idMap;
    Map<Integer, String> reverseMap;

    @Deprecated int numStates;
    SequenceType sequenceType; // encapsulate stateCount, ambiguousState, and getChar() ...

    @Deprecated
    public AbstractAlignment(int ntaxa, int nchar, Map<String, Integer> idMap, int numStates) {
        super(ntaxa, nchar);
        this.idMap = idMap;
        fillRevMap();

        // sequenceType = DataType.guessSequenceType(numStates);
        sequenceType = null;
        this.numStates = numStates;
    }

    public AbstractAlignment(int ntaxa, int nchar, Map<String, Integer> idMap, SequenceType sequenceType) {
        super(ntaxa, nchar);
        this.idMap = idMap;
        fillRevMap();

        this.sequenceType = sequenceType;
        this.numStates = sequenceType.getCanonicalStateCount();
    }

    /**
     * Copy constructor of AbstractAlignment
     */
    public AbstractAlignment(final AbstractAlignment source) {
        this.ntaxa = Objects.requireNonNull(source).ntaxa();
        this.nchar = source.nchar();
        this.idMap = new TreeMap<>(source.idMap);
        fillRevMap();

        this.sequenceType = source.getSequenceType();
    }

    public abstract int n();
    public abstract int L();

    public abstract int getState(int taxon, int position);
    public abstract void setState(int taxon, int position, int state, boolean ambiguous);

    public void setState(String taxon, int position, int state, boolean ambiguous) {
        setState(idMap.get(taxon), position, state, ambiguous);
    }

    public abstract String toJSON();

    //****** Taxa ******

    protected void fillRevMap() {
        reverseMap = new TreeMap<>();
        for (String key : idMap.keySet()) {
            reverseMap.put(idMap.get(key), key);
        }
    }

    public String getId(int taxonIndex) {
        return reverseMap.get(taxonIndex);
    }

    public String[] getTaxaNames() {
        String[] taxaNames = new String[ntaxa()];
        for (int i = 0; i < taxaNames.length; i++) {
            taxaNames[i] = reverseMap.get(i);
        }
        return taxaNames;
    }

    public String[] getTaxa() {
        String[] taxa = new String[ntaxa()];
        for (int i = 0; i < ntaxa(); i++) {
            taxa[i] = reverseMap.get(i);
        }
        return taxa;
    }

    //****** Data type ******

    public SequenceType getSequenceType() {
        return sequenceType;
    }

    public String getDataTypeDescription() {
        if (sequenceType == null) { // TODO BINARY
            if (numStates == 2) return "binary";
            else throw new IllegalArgumentException("Please use SequenceType !");
        }
        return sequenceType.getName();
    }


    //****** view ******

    @Override
    public JComponent getComponent(Value<AbstractAlignment> value) {

//        if ( DataType.isSame(DataType.BINARY, sequenceType) )
        if (numStates == 2) // TODO BINARY
            return new AlignmentComponent(value, AlignmentComponent.BINARY_COLORS);
        else return new AlignmentComponent(value, AlignmentComponent.DNA_COLORS);
    }

}
