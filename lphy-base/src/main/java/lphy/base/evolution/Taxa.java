package lphy.base.evolution;

import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.graphicalmodel.components.GeneratorCategory;
import lphy.core.graphicalmodel.components.MethodInfo;
import lphy.core.graphicalmodel.components.MultiDimensional;
import lphy.core.graphicalmodel.components.TypeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An interface that taxa-dimensioned objects can implement, such as Alignment and TimeTree.
 */
@TypeInfo(description = "An interface that taxa-dimensioned objects can implement, such as Alignment and TimeTree.\n" +
        "It reserves three types of metadata internally: taxa names, ages of taxa, and species.",
        examples = {"jcCoalescent.lphy","twoPartitionCoalescent.lphy"})
public interface Taxa extends MultiDimensional {

    //****** MethodInfo ******//

    @MethodInfo(description="The names of the taxa.",
            category = GeneratorCategory.TAXA_ALIGNMENT)
    default String[] taxaNames() {
        return getTaxaNames();
    }

    @MethodInfo(description = "gets the ages of these taxa as an array of doubles.")
    default Double[] ages() {
        return getAges();
    }

    @MethodInfo(description = "gets the number of taxa.")
    default int length() {
        return ntaxa();
    }

    @MethodInfo(description = "the total number of nodes (left + internal) in a binary tree with these taxa.",
            examples = {"simpleMultispeciesCoalescentTaxa.lphy"})
    default int nodeCount() {
        return 2*ntaxa()-1;
    }


    //******  ******//

    /**
     * @return the number of taxa this object has.
     */
    int ntaxa();

    /**
     * The default for this method is very inefficient and should be overridden by implementers!
     *
     * @return the i'th taxon.
     */
    default Taxon getTaxon(int i) {
        return new Taxon(i + "", 0.0);
    }

    /**
     * @return all Taxon objects in an array
     */
    default Taxon[] getTaxonArray() {
        // defensive copy.
        Taxon[] taxa = new Taxon[ntaxa()];
        for (int i = 0; i < taxa.length; i++) {
            taxa[i] = getTaxon(i);
        }
        return taxa;
    }

    /**
     * @return all Taxon objects in an array
     */
    default Taxon[] extantTaxa() {
        List<Taxon> taxonList = new ArrayList<>();
        for (Taxon taxon : getTaxonArray()) {
            if (taxon.isExtant()) taxonList.add(taxon);
        }
        return taxonList.toArray(new Taxon[0]);
    }


    /**
     * @return the names of the taxa.
     */
    default String[] getTaxaNames() {

        String[] taxaNames = new String[ntaxa()];
        Taxon[] taxa = getTaxonArray();
        for (int i = 0; i < ntaxa(); i++) {
            taxaNames[i] = taxa[i].getName();
        }
        return taxaNames;
    }

    /**
     * @return the species of each taxon.
     */
    default String[] getSpecies() {
        Taxon[] taxa = getTaxonArray();
        String[] species = new String[ntaxa()];
        for (int i = 0; i < ntaxa(); i++) {
            species[i] = taxa[i].getSpecies();
        }
        return species;
    }

    /**
     * @return the ages of the taxa in the same order as the taxa.
     */
    default Double[] getAges() {
        Taxon[] taxa = getTaxonArray();
        Double[] ages = new Double[ntaxa()];
        for (int i = 0; i < ntaxa(); i++) {
            ages[i] = taxa[i].getAge();
        }
        return ages;
    }

    default double getAge(String taxonName) {
        Taxon[] taxa = getTaxonArray();

        int index = indexOfTaxon(taxonName);
        if (index >= 0) return taxa[index].getAge();
        throw new IllegalArgumentException("Taxon named " + taxonName + " not found");
    }


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

        Taxon[] taxa = new Taxon[n];
        for (int i = 0; i < taxa.length; i++) {
            taxa[i] = new Taxon(i + "", 0.0);
        }

        return new Taxa.Simple(taxa);
    }

    static Taxa createTaxa(Taxon[] taxa) {

        return new Taxa.Simple(taxa);
    }

    /**
     * @param ages
     * @return a set of taxa with the given ages
     */
    static Taxa createTaxa(Double[] ages) {

        Taxon[] taxa = new Taxon[ages.length];
        for (int i = 0; i < taxa.length; i++) {
            taxa[i] = new Taxon(i + "", ages[i]);
        }

        return new Taxa.Simple(taxa);
    }

    static Taxa createTaxa(Object[] objects) {

        Taxon[] taxa = new Taxon[objects.length];
        for (int i = 0; i < taxa.length; i++) {
            taxa[i] = new Taxon(objects[i].toString(), 0.0);
        }

        return new Taxa.Simple(taxa);
    }

    static Taxa createTaxa(TimeTreeNode root) {

        Taxon[] taxa = new Taxon[root.countLeaves()];
        collectTaxon(root, taxa);

        return new Taxa.Simple(taxa);
    }

    static void collectTaxon(TimeTreeNode node, Taxon[] taxa) {
        if (node.isLeaf()) {
            taxa[node.getIndex()] = new Taxon(node.getId(), node.getAge());
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                collectTaxon(child, taxa);
            }
        }
    }

    /**
     * Legacy code of using {@code Map<String, Integer>} idMap
     * @param idMap    {@code Map<String, Integer>}
     * @return   Taxa
     */
    static Taxa createTaxa(Map<String, Integer> idMap) {

        Taxon[] taxa = new Taxon[idMap.size()];
        for (Map.Entry<String, Integer> entry : idMap.entrySet()) {
            taxa[entry.getValue()] = new Taxon(entry.getKey());
        }

        return new Taxa.Simple(taxa);
    }

    class Simple implements Taxa {

        Taxon[] taxa;

        public Simple(Taxon[] taxa) {
            this.taxa = taxa;
        }

        @Override
        public int ntaxa() {
            return taxa.length;
        }

        @Override
        public Taxon getTaxon(int i) {
            return taxa[i];
        }
    }
}
