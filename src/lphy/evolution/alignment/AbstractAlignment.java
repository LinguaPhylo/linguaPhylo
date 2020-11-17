package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.app.AlignmentColour;
import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.evolution.sequences.DataType;
import lphy.graphicalModel.MethodInfo;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**TODO TaxaData implements Taxa ?
 * The abstract class defines everything related to Taxa, Data type, but except of sequences.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public abstract class AbstractAlignment implements Alignment {

    // may not have sequences
    protected int nchar;

    protected Taxa taxa;

    @Deprecated int numStates;
    SequenceType sequenceType; // encapsulate stateCount, ambiguousState, and getChar() ...


    /**
     * Init alignment with taxa and number of site.
     * @param idMap
     * @param nchar
     */
    public AbstractAlignment(Map<String, Integer> idMap, int nchar) {
        this.nchar = nchar;
        this.taxa = Taxa.createTaxa(idMap);
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
     * @param taxa    {@link Taxa.Simple}.
     * @param nchar   the number of sites.
     * @param sequenceType  {@link SequenceType}
     */
    public AbstractAlignment(Taxa taxa, int nchar, SequenceType sequenceType) {
        this.taxa = taxa; // Arrays.copyOf ?
        this.nchar = nchar;
        this.sequenceType = sequenceType;
        this.numStates = sequenceType.getCanonicalStateCount();
    }

    /**
     * Copy constructor, where nchar input allows partition to create from the parent Alignment
     */
    public AbstractAlignment(int nchar, final AbstractAlignment source) {
        this.nchar = nchar;
        // deep copy
        this.taxa = Taxa.createTaxa(Arrays.copyOf(source.getTaxa().getTaxonArray(), source.ntaxa()));

        this.sequenceType = source.getSequenceType();
        if (sequenceType == null)
            this.numStates = source.numStates;
    }

    /**
     * @see #AbstractAlignment(int, AbstractAlignment)
     */
    public AbstractAlignment(final AbstractAlignment source) {
        this(Objects.requireNonNull(source).nchar(), source);
    }

    public abstract boolean hasParts();


    //****** Sites ******
    @MethodInfo(description="The number of characters/sites in this alignment.")
    public int nchar() {
        return nchar;
    }

    //****** Taxa ******

    @Override
    public int ntaxa() {
        return taxa.ntaxa();
    }

    @Override
    public Taxon getTaxon(int taxonIndex) {
        return taxa.getTaxon(taxonIndex);
    }

    /**
     * This shares the same index with ages[]
     * @param taxonIndex  the index of a taxon
     * @return     the name of this taxon
     */
    public String getTaxonName(int taxonIndex) {
        return getTaxon(taxonIndex).getName();
    }

    @Override
    public String[] getTaxaNames() {
        return taxa.getTaxaNames();
    }

    @Override
    public Taxon[] getTaxonArray() {
        return taxa.getTaxonArray();
    }

//    @Override
//    public int indexOfTaxon(String taxon) {
//        return getTaxaNames();
//    }

    public String toString() {
        return sequenceType.getName() + " alignment " + ntaxa() + " by " + nchar;
    }

    //****** Data type ******

    @Override
    public SequenceType getSequenceType() {
        return sequenceType;
    }

    @Override
    public int getNumOfStates() {
        if (sequenceType != null) {
            sequenceType.getCanonicalStateCount();
        }
        return numStates;
    }

    public String getSequenceTypeStr() {
        if (sequenceType == null) { // TODO
            if (numStates == 2) return "binary";
            else return "standard";
//                throw new IllegalArgumentException("Please define SequenceType, not numStates !");
        }
        return sequenceType.getName();
    }


    //****** view ******

    public Color[] getColors() {
//        if ( DataType.isSame(DataType.BINARY, sequenceType) )
        if (numStates == 2) // TODO
            return AlignmentColour.BINARY_COLORS;
        else if ( DataType.isSame(DataType.NUCLEOTIDE, sequenceType) )
            return AlignmentColour.DNA_COLORS;
        else if ( DataType.isSame(DataType.AMINO_ACID, sequenceType) )
            return AlignmentColour.PROTEIN_COLORS;
        else if ( numStates <=  4 ) // for traits
            return AlignmentColour.DNA_COLORS;
        else if ( numStates <=  20 ) // TODO
            return AlignmentColour.PROTEIN_COLORS;
        else throw new IllegalArgumentException("Cannot choose colours given data type " +
                    sequenceType + " and numStates " + numStates + " !");
    }

    /**
     * @return  state, if 0 <= state < numStates (no ambiguous),
     *          otherwise return numStates which is the last index
     *          in colours always for ambiguous state.
     */
    public int getColourIndex(int state) {
        //TODO state criteria not hard code
        if (numStates == 2 && state > 1) // TODO BINARY data type
            return 2;
        if (DataType.isType(this, SequenceType.NUCLEOTIDE) && state > 3)
            return 4;
        else if (DataType.isType(this, SequenceType.AMINO_ACID) && state > 19) // no ambiguous
            //TODO why jebl make AMINO_ACID 22 ?
            return 20; // the last extra is always for ambiguous
        return state;
    }

    /**
     * This shares the same index with {@link #getTaxaNames()}
     */

    @Override
    public boolean hasAges() {
        for (Taxon taxon : getTaxonArray())
            if (taxon.getAge() > 0) return true;
        return false;
    }

    @Override
    public int getDimension() {
        return ntaxa();
    }
}
