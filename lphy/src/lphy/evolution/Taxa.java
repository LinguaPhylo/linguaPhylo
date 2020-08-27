package lphy.evolution;

import java.util.ArrayList;
import java.util.List;

/**
 * An interface that taxa-dimensioned objects can implement, such as Alignment and TimeTree.
 */
public interface Taxa {

    /**
     * @return the number of taxa this object has.
     */
    int ntaxa();

    default String[] getTaxa() {
        String[] taxa = new String[ntaxa()];
        for (int i = 0; i < ntaxa(); i++) {
            taxa[i] = "" + i;
        }
        return taxa;
    }
}
