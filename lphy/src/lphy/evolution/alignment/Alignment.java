package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.NChar;
import lphy.evolution.Taxa;

/**
 * For all kind of alignments.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public interface Alignment extends Taxa, NChar {

    /**
     * @param taxon      the index of taxon.
     * @param position   the site position.
     * @return  the integer state at the given coordinate of this alignment.
     */
    int getState(int taxon, int position);

    /**
     * Set integer states to the alignment.
     * @param taxon      the index of taxon.
     * @param position   the site position.
     * @param state      the state in integer.
     */
    void setState(int taxon, int position, int state);

    /**
     * @return the number of sites.
     */
    @Override
    int nchar();

    /**
     * @return the number of taxa.
     */
    @Override
    int ntaxa();

    /**
     * @return get taxa names.
     */
    String[] getTaxa();

    /**
     * @return get data types.
     */
    SequenceType getSequenceType();

}
