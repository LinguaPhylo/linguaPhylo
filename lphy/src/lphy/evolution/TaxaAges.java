package lphy.evolution;

import lphy.graphicalModel.Multidimensional;

/**
 * An interface that taxa-dimensioned objects with age information.
 */
public interface TaxaAges extends Taxa, Multidimensional {

    /**
     * @return the ages of the taxa in the same order as the taxa.
     */
    Double[] getAges();

    default double getAge(String taxon) {
        String[] taxa = getTaxa();

        for (int i =0; i < taxa.length; i++) {
            if (taxa[i].equals(taxon)) return getAges()[i];
        }
        throw new IllegalArgumentException("Taxon named " + taxon + " not found");
    }
}
