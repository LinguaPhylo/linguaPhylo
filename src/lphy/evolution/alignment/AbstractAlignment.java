package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.app.AlignmentColour;
import lphy.app.AlignmentComponent;
import lphy.app.HasComponentView;
import lphy.evolution.Taxon;
import lphy.evolution.sequences.DataType;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * The abstract class defines everything related to Taxa, Data type, but except of sequences.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public abstract class AbstractAlignment implements Alignment, HasComponentView<AbstractAlignment> {

    // may not have sequences
    protected int nchar;

    Map<String, Integer> idMap;
    Map<Integer, String> reverseMap;

    @Deprecated int numStates;
    SequenceType sequenceType; // encapsulate stateCount, ambiguousState, and getChar() ...

    // same index as Map<Integer, String> reverseMap
    Map<Integer, Taxon> taxonMap; // TODO duplicate to reverseMap ?

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
     * {@link Taxon} stores name, age, sepices.
     * @param taxa    the array index will use as the key in {@link #taxonMap}.
     * @param nchar   the number of sites.
     * @param sequenceType  {@link SequenceType}
     */
    public AbstractAlignment(Taxon[] taxa, int nchar, SequenceType sequenceType) {
        this.nchar = nchar;
        this.sequenceType = sequenceType;
        this.numStates = sequenceType.getCanonicalStateCount();

        taxonMap = new TreeMap<>();
        for (int i = 0; i < taxa.length; i++)
            taxonMap.put(i, taxa[i]);

        // fill idMap
        idMap = new TreeMap<>();
        for (int i = 0; i < taxa.length; i++)
            idMap.put(taxa[i].getName(), i);
        fillRevMap();
    }

    /**
     * Copy constructor, where nchar input allows partition to create from the parent Alignment
     */
    public AbstractAlignment(int nchar, final AbstractAlignment source) {
        this.nchar = nchar;
        this.idMap = new TreeMap<>(Objects.requireNonNull(source).idMap);
        fillRevMap();

        this.sequenceType = source.getSequenceType();
        if (source.taxonMap != null)
            this.taxonMap = new LinkedHashMap<>(source.taxonMap);
    }

    /**
     * @see #AbstractAlignment(int, AbstractAlignment)
     */
    public AbstractAlignment(final AbstractAlignment source) {
        this(Objects.requireNonNull(source).nchar(), source);
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

    @Override
    public int indexOfTaxon(String taxon) {
        return idMap.get(taxon);
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

    /**
     * This shares the same index with {@link #getTaxaNames()}
     */
    @Override
    public Double[] getAges() {
        Double[] ages = new Double[ntaxa()];

        if (taxonMap == null) {
            Arrays.fill(ages, 0.0);
            return ages;
        }

        for (int i = 0; i < ntaxa(); i++) {
            Taxon taxon = getTaxon(i);
            if (taxon != null) ages[i] = taxon.getAge();
        }
        return ages;
    }

    public Taxon getTaxon(int taxonIndex) {
        return taxonMap.get(taxonIndex);
    }

    @Override
    public Taxon[] getTaxonArray() {
        if (taxonMap == null) return Alignment.super.getTaxonArray();
        return taxonMap.values().toArray(Taxon[]::new);
    }

    @Override
    public String[] getSpecies() {
        return Alignment.super.getSpecies();
    }

    @Override
    public int getDimension() {
        return ntaxa();
    }
}
