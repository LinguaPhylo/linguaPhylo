package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.NChar;
import lphy.evolution.Taxa;

/**
 * For all kind of alignments.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public interface Alignment extends Taxa, TaxaCharacterMatrix<Integer> {

    /**
     * @param taxon      the name of the taxon.
     * @param position   the site position.
     * @return  the integer state at the given coordinate of this alignment.
     */
    default Integer getState(String taxon, int position) {
        return getState(indexOfTaxon(taxon), position);
    }

    /**
     * @param taxon      the index of taxon.
     * @param position   the site position.
     * @return  the integer state at the given coordinate of this alignment.
     */
    int getState(int taxon, int position);

    /**
     * @return get data types.
     */
    SequenceType getSequenceType();

    default Class getComponentType() {
        return Integer.class;
    }

    default Taxa getTaxa() {
        return this;
    }
}
