package lphybeast.tobeast.data;

import beast.core.BEASTInterface;
import beast.core.MCMC;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.alignment.Taxon;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * @author Walter Xie
 */
public class DataExchanger {

    protected beast.evolution.alignment.Alignment beastAlignment = null;


    public DataExchanger(beast.evolution.alignment.Alignment beastAlignment) {
//        this.beastAlignment = beastAlignment;
        this.beastAlignment = getAlignment();
    }

    //TODO from nex
    public Alignment getAlignment() {
        Sequence human = new Sequence("0human", "AAAACCCCGGGGTTTT");
        Sequence chimp = new Sequence("1chimp", "ACGTACGTACGTACGT");
        Sequence gorilla = new Sequence("2gorilla", "ATGTACGTACGTACTT");

        Alignment data = new Alignment();
        data.initByName("sequence", human, "sequence", chimp, "sequence", gorilla,
                "dataType", "nucleotide"
        );
        return data;
    }


    public void replaceTaxaNamesByOrder(TimeTree timeTree) {
        List<String> beastTaxaNm = beastAlignment.getTaxaNames();
        int i= 0;
        for (TimeTreeNode node : timeTree.getNodes()) {
            if (node.isLeaf()) {
                node.setId(beastTaxaNm.get(i));
                i++;
            }
        }
    }

// TODO map taxa names


    //*** in dev ***//

    public void replaceAlignment(final Set<BEASTInterface> elements, final SortedMap<String, Taxon> allTaxa) {
        Alignment alg = null;
        for (BEASTInterface bo : elements) {
            if (bo instanceof Alignment) {
                Alignment newAlg = getAlignment();
                List<Sequence> newSeqs = newAlg.sequenceInput.get();
                alg = (Alignment) bo;

                // validate SiteCount
                if (newAlg.getSiteCount() != alg.getSiteCount()) {
                    System.err.println("Warning: the number of sites ("+ alg.getSiteCount() +
                            ") defined in LPhy != " + " sites (" + newAlg.getSiteCount() +
                            ") in the given alignment !");
                }

                alg.sequenceInput.get().clear();
                // this will add list, not replace
                alg.sequenceInput.setValue(newSeqs, alg);
                alg.initAndValidate();

            }
        }

        assert alg != null;

        replaceAllTaxaBy(allTaxa, alg); // TODO cannot access taxonset in Tree

    }

    public void replaceTaxonSet(MCMC mcmc) {

//        mcmc.startStateInput.get().stateNodeInput.get();

    }



    private void replaceAllTaxaBy(final SortedMap<String, Taxon> allTaxa, final Alignment alg) {
        assert allTaxa.size() == alg.getTaxonCount();

        List<String> tn = alg.getTaxaNames();
        allTaxa.clear();//TODO
        for (String taxonID : tn) {
            if (!allTaxa.containsKey(taxonID)) {
                allTaxa.put(taxonID, new Taxon(taxonID));
            }
        }



        for (Map.Entry<String, Taxon> pair : allTaxa.entrySet()) {
//             pair.getKey()  pair.getValue();
        }

    }


}
