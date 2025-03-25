package lphy.base.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.CellPosition;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.core.model.annotation.MethodInfo;

import java.util.*;

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
    final SequenceType sequenceType;


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

    public AbstractAlignment(Alignment root, Map<CellPosition, Integer> variantStore) {
        this.sequenceType = root.getSequenceType();
        this.nchar = root.nchar();

        // calculate how many taxa in variantStore
        List<String> names = new ArrayList<>();
        for (CellPosition cellPosition: variantStore.keySet()){
            String name = cellPosition.getCellName();
            if (! names.contains(name)){
                names.add(name);
            }
        }

        this.taxa = Taxa.createTaxa(names.size());
    }

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

    @Override
    public int getDimension() {
        return ntaxa();
    }
}
