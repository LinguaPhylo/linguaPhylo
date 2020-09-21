package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.app.AlignmentColour;
import lphy.app.AlignmentComponent;
import lphy.app.HasComponentView;
import lphy.evolution.Taxa;
import lphy.evolution.sequences.DataType;
import lphy.graphicalModel.Value;
import scala.Array;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Everything related to Taxa, Data type
 * @author Alexei Drummond
 * @author Walter Xie
 */
public abstract class AbstractAlignment implements Alignment, Taxa, HasComponentView<AbstractAlignment> {

    // may not have sequences
    protected int nchar;

    Map<String, Integer> idMap;
    Map<Integer, String> reverseMap;

    @Deprecated int numStates;
    SequenceType sequenceType; // encapsulate stateCount, ambiguousState, and getChar() ...

    Map<String, Double> ageMap;

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
        if (source.ageMap != null)
            this.ageMap = new LinkedHashMap<>(source.ageMap);
    }

    protected void fillRevMap() {
        reverseMap = new TreeMap<>();
        for (String key : idMap.keySet()) {
            reverseMap.put(idMap.get(key), key);
        }
    }


    public abstract String toJSON();

    public abstract boolean hasParts();


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

    /**
     * This shares the same index with ages[]
     * @param taxonIndex  the index of a taxon
     * @return     the name of this taxon
     */
    public String getTaxonName(int taxonIndex) {
        return reverseMap.get(taxonIndex);
    }

    @Override
    public String[] getTaxaNames() {
        String[] taxa = new String[ntaxa()];
        for (int i = 0; i < ntaxa(); i++) {
            taxa[i] = getTaxonName(i);
        }
        return taxa;
    }

    /**
     * @param taxon  Case sensitive
     * @return   whether this alignment has the given taxon name
     */
    public boolean hasTaxon(String taxon) {
        return idMap.containsKey(taxon);
    }

    public String toString() {
        return ntaxa() + " by " + nchar;
    }

    //****** Data type ******

    @Override
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

    //****** ages ******

    public void setAgeMap(final Map<String, Double> ageMap) {
        for (String taxon : Objects.requireNonNull(ageMap).keySet()) {
            if (!hasTaxon(taxon))
                throw new IllegalArgumentException("Taxon " + taxon + " not exist in the alignment !");
        }
        this.ageMap = ageMap;
    }

    /**
     * This shares the same index with {@link #getTaxaNames()}
     */
    @Override
    public Double[] getAges() {
        Double[] ages = new Double[ntaxa()];

        if (ageMap == null) {
            Arrays.fill(ages, 0.0);
            return ages;
        }

        for (int i = 0; i < ntaxa(); i++) {
            Double age = ageMap.get(getTaxonName(i));
            if (age == null)
                throw new IllegalArgumentException("Invalid age for taxon " + getTaxonName(i) + " at index " + i);
            ages[i] = age;
        }
        return ages;
    }

    @Override
    public int getDimension() {
        return ntaxa();
    }
}
