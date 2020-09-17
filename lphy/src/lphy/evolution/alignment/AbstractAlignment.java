package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.app.AlignmentColour;
import lphy.app.AlignmentComponent;
import lphy.app.HasComponentView;
import lphy.evolution.NChar;
import lphy.evolution.Taxa;
import lphy.evolution.sequences.DataType;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Everything related to Taxa, Data type
 * @author Walter Xie
 */
public abstract class AbstractAlignment implements Taxa, NChar, HasComponentView<AbstractAlignment> {

    // may not have sequences
    protected int nchar;

    Map<String, Integer> idMap;
    Map<Integer, String> reverseMap;

    @Deprecated int numStates;
    SequenceType sequenceType; // encapsulate stateCount, ambiguousState, and getChar() ...

    /**
     * Init alignment with taxa and number of site.
     * @param idMap
     * @param nchar
     */
    public AbstractAlignment(Map<String, Integer> idMap, int nchar) {
        this.nchar = nchar;
        this.idMap = idMap;
        fillRevMap();
    }

    @Deprecated
    public AbstractAlignment(Map<String, Integer> idMap, int nchar, int numStates) {
        this(idMap, nchar);
        // sequenceType = DataType.guessSequenceType(numStates);
        sequenceType = null;
        this.numStates = numStates;
    }

    public AbstractAlignment(Map<String, Integer> idMap, int nchar, SequenceType sequenceType) {
        this(idMap, nchar);
        this.sequenceType = sequenceType;
        this.numStates = sequenceType.getCanonicalStateCount();
    }

    /**
     * Copy constructor of AbstractAlignment
     */
    public AbstractAlignment(final AbstractAlignment source) {
        this.nchar = Objects.requireNonNull(source).nchar();
        this.idMap = new TreeMap<>(source.idMap);
        fillRevMap();

        this.sequenceType = source.getSequenceType();
    }

    //****** Sequences ******

    public abstract int n();
    public abstract int L();

    public abstract int getState(int taxon, int position);
    public abstract void setState(int taxon, int position, int state, boolean ambiguous);

    public void setState(String taxon, int position, int state, boolean ambiguous) {
        setState(idMap.get(taxon), position, state, ambiguous);
    }

    public abstract String toJSON();

    //****** Sites ******
    @Override
    public int nchar() {
        return nchar;
    }

    //****** Taxa ******

    @Override
    public int ntaxa() {
        return idMap.size();
    }

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

    public String toString() {
        return ntaxa() + " by " + nchar;
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
        return new AlignmentComponent(value);
    }

    public Color[] getColors() {
//        if ( DataType.isSame(DataType.BINARY, sequenceType) )
        if (numStates == 2) // TODO BINARY
            return AlignmentColour.BINARY_COLORS;
        else if ( DataType.isSame(DataType.AMINO_ACID, sequenceType) )
            return AlignmentColour.PROTEIN_COLORS;
        else return AlignmentColour.DNA_COLORS;
    }

    /**
     * @return  state, if 0 <= state < numStates (no ambiguous),
     *          otherwise return numStates which is the last index in colours always for ambiguous state.
     */
    public int getColorByState(int state) {
        //TODO state criteria not hard code
        if (numStates == 2 && state > 1) // TODO BINARY data type
            return 2;
        if (DataType.isSame(SequenceType.NUCLEOTIDE, getSequenceType()) && state > 3)
            return 4;
        else if (DataType.isSame(SequenceType.AMINO_ACID, getSequenceType()) && state > 19) // no ambiguous
            //TODO why jebl make AMINO_ACID 22 ?
            return 20; // the last extra is always for ambiguous
        return state;
    }

    public abstract boolean hasParts();
}
