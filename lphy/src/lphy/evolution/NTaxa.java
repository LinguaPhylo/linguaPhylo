package lphy.evolution;

/**
 * An interface that taxa-dimensioned objects can implement, such as Alignment and TimeTree.
 */
public interface NTaxa {

    /**
     * @return the number of taxa this object has.
     */
    int ntaxa();
}
