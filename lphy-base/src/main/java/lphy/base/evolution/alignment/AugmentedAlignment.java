package lphy.base.evolution.alignment;

/**
 * Guide to create an alignment containing the internal node sequences.
 */
public interface AugmentedAlignment<T> extends TaxaCharacterMatrix<T> {

    /**
     * @param sequenceId the index of sequences, tips are from 0 to (ntaxa - 1),
     *                   internal nodes are the rest, and root index is the last.
     * @param position   the site position.
     * @return  the integer state at the given coordinate of this alignment.
     */
    T getState(int sequenceId, int position);

}
