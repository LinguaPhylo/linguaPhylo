package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.evolution.alignment.Taxon;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.speciation.GeneTreeForSpeciesTreeDistribution;
import beast.evolution.speciation.SpeciesTreePopFunction;
import beast.evolution.tree.Tree;
import lphy.evolution.coalescent.MultispeciesCoalescent;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

import java.util.ArrayList;
import java.util.List;

public class MultispeciesCoalescentToBEAST implements GeneratorToBEAST<MultispeciesCoalescent> {
    @Override
    public BEASTInterface generatorToBEAST(MultispeciesCoalescent generator, BEASTInterface value, BEASTContext context) {

        GeneTreeForSpeciesTreeDistribution starbeast = new GeneTreeForSpeciesTreeDistribution();

        Tree speciesTree = (Tree) context.getBEASTObject(generator.getSpeciesTree());
        Tree geneTree = (Tree) value;

        // This is the mapping from gene tree taxa to species tree taxa
        TaxonSet taxonSuperSet = speciesTree.getTaxonset();
        List<Taxon> spTaxonSets = new ArrayList<>();
        for (int sp = 0; sp < generator.getN().value().length; sp++) {
            String speciesId = sp + "";
            Taxon spTaxon = taxonSuperSet.getTaxon(speciesId);
            TaxonSet spTaxonSet;
            List<Taxon> geneTaxonList = new ArrayList<>();

            if (spTaxon instanceof TaxonSet) {
                spTaxonSet = (TaxonSet) spTaxon;
                System.out.println("Adding existing taxa: " + spTaxonSet.getTaxonSet().size());
                geneTaxonList.addAll(spTaxonSet.getTaxonSet());
            } else {
                spTaxonSet = new TaxonSet();
            }

            for (int k = 0; k < generator.getN().value()[sp]; k++) {
                String id = sp + MultispeciesCoalescent.separator + k;
                Taxon toAdd = geneTree.getTaxonset().getTaxon(id);
                if (!containsId(geneTaxonList, id)) {
                    geneTaxonList.add(toAdd);
                }
            }

            if (spTaxonSet.taxonsetInput.get() != null) {
                spTaxonSet.taxonsetInput.get().clear();
            }
            spTaxonSet.setInputValue("taxon", geneTaxonList);
            spTaxonSet.initAndValidate();
            spTaxonSet.setID(speciesId);
            spTaxonSets.add(spTaxonSet);
        }
        if (taxonSuperSet.taxonsetInput.get() != null) {
            taxonSuperSet.taxonsetInput.get().clear();
        }
        taxonSuperSet.setInputValue("taxon", spTaxonSets);
        taxonSuperSet.initAndValidate();

        speciesTree.setInputValue("taxonset", taxonSuperSet);
        speciesTree.initAndValidate();

        starbeast.setInputValue("speciesTree", speciesTree);
        starbeast.setInputValue("tree", geneTree);

        SpeciesTreePopFunction speciesTreePopFunction = new SpeciesTreePopFunction();
        speciesTreePopFunction.setInputValue("tree", speciesTree);
        speciesTreePopFunction.setInputValue("bottomPopSize", context.getBEASTObject(generator.getPopulationSizes()));
        speciesTreePopFunction.setInputValue("taxonset", taxonSuperSet);

        speciesTreePopFunction.initAndValidate();

        starbeast.setInputValue("speciesTreePrior", speciesTreePopFunction);
        starbeast.initAndValidate();

        return starbeast;
    }

    private boolean containsId(List<Taxon> taxonList, String id) {
        for (Taxon taxon : taxonList) {
            if (taxon.getID().equals(id)) return true;
        }
        return false;
    }

    @Override
    public Class<MultispeciesCoalescent> getGeneratorClass() {
        return MultispeciesCoalescent.class;
    }
}
