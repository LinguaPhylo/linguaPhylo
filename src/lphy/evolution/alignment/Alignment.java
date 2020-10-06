package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.Taxa;

import java.awt.*;

/**
 * An alignment of discrete character states, returned as Integers.
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

    int getNumOfStates();

    default Class getComponentType() {
        return Integer.class;
    }

    /**
     * @param state  the given state of sequences
     * @return   A colour index for that state
     */
    int getColourIndex(int state);

    /**
     * @return   The colours for states of sequences.
     */
    Color[] getColors();


    //****** Taxa ******//

    default Taxa getTaxa() {
        return this;
    }

    /**
     * @param taxonIndex  the index of a taxon
     * @return            the name of this taxon
     */
    String getTaxonName(int taxonIndex);


}
