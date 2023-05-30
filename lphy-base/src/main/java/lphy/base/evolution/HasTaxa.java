package lphy.base.evolution;

/**
 * For everything has taxa, and getTaxa when needed.
 */
public interface HasTaxa {
    /**
     * @return a taxa object describing the taxa.
     */
    Taxa getTaxa();
}
