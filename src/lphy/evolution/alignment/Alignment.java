package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.Taxa;

import java.awt.*;
import java.util.Objects;

/**
 * An alignment of discrete character states, returned as Integers.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public interface Alignment extends Taxa, TaxaCharacterMatrix<Integer> {

    //****** states ******//

    /**
     * Set int states.
     * @param taxon      the index of taxon in the 1st dimension.
     * @param position   the site position in the 2nd dimension.
     * @param state      the state in integer
     */
    void setState(int taxon, int position, int state);

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


    //****** data type ******//

    /**
     * @return get data types.
     */
    SequenceType getSequenceType();

    /**
     * @return name of data type
     */
    default String getSequenceTypeStr() {
        return Objects.requireNonNull(getSequenceType()).getName();
    }

    /**
     * @return num of states no ambiguous
     */
    default int getCanonicalStateCount() {
        return Objects.requireNonNull(getSequenceType()).getCanonicalStateCount();
    }

    /**
     * @return number of states including ambiguous states
     */
    default int getStateCount() {
        return Objects.requireNonNull(getSequenceType()).getStateCount();
    }


    //****** Taxa ******//

    default Taxa getTaxa() {
        return this;
    }

    /**
     * @param taxonIndex  the index of a taxon
     * @return            the name of this taxon
     */
    String getTaxonName(int taxonIndex);

    boolean hasAges();


    //****** Others ******//

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

}
