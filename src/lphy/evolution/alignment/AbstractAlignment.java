package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.app.AlignmentColour;
import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.evolution.datatype.Binary;
import lphy.evolution.datatype.DataType;
import lphy.graphicalModel.MethodInfo;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * The abstract class defines everything related to Taxa, Data type, but except of sequences.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public abstract class AbstractAlignment implements Alignment {

    // may not have sequences
    protected int nchar;
    protected Taxa taxa;

    // encapsulate stateCount, ambiguousState, and getChar() ...
    SequenceType sequenceType;


    /**
     * for simulated alignment
     * @param idMap
     * @param nchar
     * @param sequenceType
     */
    public AbstractAlignment(Map<String, Integer> idMap, int nchar, SequenceType sequenceType) {
        this.taxa = Taxa.createTaxa(idMap);
        this.nchar = nchar;
        this.sequenceType = sequenceType;
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
    }

    /**
     * Copy constructor, where nchar input allows partition to create from the parent Alignment
     */
    public AbstractAlignment(int nchar, final Alignment source) {
        this.nchar = nchar;
        // deep copy
        this.taxa = Taxa.createTaxa(Arrays.copyOf(source.getTaxa().getTaxonArray(), source.ntaxa()));

        this.sequenceType = source.getSequenceType();
    }

    public AbstractAlignment(final AbstractAlignment source) {
        this(Objects.requireNonNull(source).nchar(), source);
    }

//    public abstract boolean hasParts();


    //****** MethodInfo ******

    @MethodInfo(description="The number of characters/sites.", narrativeName = "number of characters")
    public Integer nchar() {
        return nchar;
    }

    @Override
    @MethodInfo(description="The names of the taxa.")
    public String[] getTaxaNames() {
        return taxa.getTaxaNames();
    }

    @MethodInfo(description = "the taxa of the alignment.", narrativeName = "list of taxa")
    public Taxa taxa() {
        return getTaxa();
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
    public Taxon[] getTaxonArray() {
        return taxa.getTaxonArray();
    }

    public Taxa getTaxa() {
        return taxa;
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
        return Objects.requireNonNull(sequenceType);
    }

    //****** view ******

    public Color[] getColors() {
        if ( DataType.isSame(Binary.getInstance(), sequenceType) )
            return AlignmentColour.BINARY_COLORS;
        else if ( DataType.isSame(DataType.NUCLEOTIDE, sequenceType) )
            return AlignmentColour.DNA_COLORS;
        else if ( DataType.isSame(DataType.AMINO_ACID, sequenceType) )
            return AlignmentColour.PROTEIN_COLORS;
        //*** other types ***//
        else if ( getCanonicalStateCount() <=  4 ) // for traits
            return AlignmentColour.DNA_COLORS;
        else if ( getCanonicalStateCount() <=  20 ) // TODO
            return AlignmentColour.PROTEIN_COLORS;
        else throw new IllegalArgumentException("Cannot choose colours given data type " +
                    sequenceType + " and numStates " + getCanonicalStateCount() + " !");
    }

    /**
     * @return  state, if 0 <= state < numStates (no ambiguous),
     *          otherwise return numStates which is the last index
     *          in colours always for ambiguous state.
     */
    public int getColourIndex(int state) {
        if (DataType.isType(this, Binary.getInstance()) && state > 1 )
            return 2;
        if (DataType.isType(this, SequenceType.NUCLEOTIDE) && state > 3)
            return 4;
        else if (DataType.isType(this, SequenceType.AMINO_ACID) && state > 19) // no ambiguous
            //TODO why jebl make AMINO_ACID 22 ?
            return 20; // the last extra is always for ambiguous
        return state;
    }


    @Override
    public int getDimension() {
        return ntaxa();
    }
}
