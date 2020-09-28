package lphy.evolution;

import lphy.graphicalModel.MultiDimensional;

import java.util.Arrays;

/**
 * An interface that taxa-dimensioned objects can implement, such as Alignment and TimeTree.
 */
public interface Taxa extends MultiDimensional {

    /**
     * @return the number of taxa this object has.
     */
    int ntaxa();

    /**
     * @param taxon
     * @return the index of this taxon name, or -1 if this taxon name is not in this taxa object.
     */
    default int indexOfTaxon(String taxon) {
        String[] names = getTaxaNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(taxon)) return i;
        }
        return -1;
    }

    /**
     * @return the names of the taxa.
     */
    default String[] getTaxaNames() {
        String[] taxa = new String[ntaxa()];
        for (int i = 0; i < ntaxa(); i++) {
            taxa[i] = "" + i;
        }
        return taxa;
    }

    /**
     * @return the species of each taxon.
     */
    default String[] getSpecies() {
        return getTaxaNames();
    }

    default double getAge(String taxon) {
        String[] taxa = getTaxaNames();

        for (int i =0; i < taxa.length; i++) {
            if (taxa[i].equals(taxon)) return getAges()[i];
        }
        throw new IllegalArgumentException("Taxon named " + taxon + " not found");
    }

    /**
     * @return the ages of the taxa in the same order as the taxa.
     */
    default Double[] getAges() {
        Double[] ages = new Double[ntaxa()];
        Arrays.fill(ages, 0.0);
        return ages;
    }

    /**
     * @return an array of taxon objects.
     */
    default Taxon[] getTaxonArray() {

        String[] names = getTaxaNames();
        String[] species = getSpecies();
        Double[] ages = getAges();

        Taxon[] taxa = new Taxon[names.length];

        for (int i = 0; i < taxa.length; i++) {
            taxa[i] = new Taxon(names[i], species[i], ages[i]);
        }
        return taxa;
    }

    /**
     * @return true if all taxa ages are equal, otherwise false.
     */
    default boolean isUltrametric() {
        Double[] ages = getAges();
        for (int i = 1; i < ages.length; i++) {
            if (!ages[i].equals(ages[0])) return false;
        }
        return true;
    }

    default int getDimension() {
        return ntaxa();
    }

    static Taxa createTaxa(int n) {
        Double[] ages = new Double[n];
        Arrays.fill(ages, 0.0);

        return new Taxa() {
            @Override
            public int ntaxa() {
                return n;
            }

            @Override
            public Double[] getAges() {
                return ages;
            }
        };
    }

    static Taxa createTaxa(Taxon[] taxa) {

        String[] names = new String[taxa.length];
        String[] species = new String[taxa.length];
        Double[] ages = new Double[taxa.length];

        for (int i = 0; i < taxa.length; i++) {
            names[i] = taxa[i].getName();
            species[i] = taxa[i].getSpecies();
            ages[i] = taxa[i].getAge();
        }

        return new Taxa() {
            @Override
            public int ntaxa() {
                return taxa.length;
            }

            @Override
            public Double[] getAges() {
                return ages;
            }

            @Override
            public String[] getTaxaNames() {
                return names;
            }

            @Override
            public String[] getSpecies() {
                return species;
            }

            @Override
            public Taxon[] getTaxonArray() {
                return taxa;
            }
        };
    }

    /**
     * @param ages
     * @return a set of taxa with the given ages
     */
    static Taxa createTaxa(Double[] ages) {

        String[] names = new String[ages.length];
        for (int i = 0; i < ages.length; i++) {
            names[i] = i + "";
        }

        return new Taxa() {
            @Override
            public int ntaxa() {
                return ages.length;
            }

            @Override
            public Double[] getAges() {
                return ages;
            }

            @Override
            public String[] getTaxaNames() {
                return names;
            }
        };
    }

    static Taxa createTaxa(Object[] taxa) {

        String[] names = new String[taxa.length];
        Double[] ages = new Double[taxa.length];

        for (int i = 0; i < taxa.length; i++) {
            names[i] = taxa[i].toString();
        }
        Arrays.fill(ages, 0.0);

        return new Taxa() {
            @Override
            public int ntaxa() {
                return taxa.length;
            }

            @Override
            public Double[] getAges() {
                return ages;
            }

            @Override
            public String[] getTaxaNames() {
                return names;
            }
        };
    }
}
