package lphybeast.tobeast.data;

import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Taxon;
import beast.util.NexusParser;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author Walter Xie
 */
public class DataExchanger {


    final protected NexusParser nexusParser;
    final protected beast.evolution.alignment.Alignment beastAlignment;

    Pattern pattern;
    Map<String,String> args;

    public DataExchanger(NexusParser nexusParser) {
        this.nexusParser = nexusParser;
        // must have alignment
        this.beastAlignment = nexusParser.m_alignment;
        assert beastAlignment != null;

        parse(nexusParser);
    }

    private void parse(NexusParser nexusParser) {
//            beastParser.traitSet;

        //TODO a lot parsing data to get from nex
//            beastParser.taxa;
//            beastParser.trees;
//            beastParser.calibrations;
//            beastParser.filteredAlignments;

        int ntaxa = getAlignment().getTaxonCount();
        int L = getAlignment().getSiteCount();

        args = new HashMap<>();
        // real data
        args.put("%ntaxa%", Integer.toString(ntaxa));
        args.put("%ages%", "");
        args.put("%L%", Integer.toString(L));

        //TODO trouble to use $ or () reserved in regx
        // Create pattern of the format "...|..."
        StringBuilder patternString = new StringBuilder();
        int i = 0;
        for (String key : args.keySet()) {
//            key = "\\$" + key + "\\$";
            if (i == 0) patternString.append(key);
            else        patternString.append("|").append(key);
            i++;
        }
        pattern = Pattern.compile(patternString.toString(), CASE_INSENSITIVE);
    }

    public Alignment getAlignment() {
        if (beastAlignment == null) throw new IllegalArgumentException("Alignment must be available !");
        return beastAlignment;
    }

    //****** before LPhyParser ******//

    // replace var in cmd
    public String assignArgsTo(String line) {
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            // Get the group matched
            String regx = matcher.group();
            String replacement = args.get(regx);
            line = line.replaceAll(regx, replacement);

            System.out.println("Find argument " + matcher.group() + " in LPhy and replace to " + replacement);
        }
        return line;
    }



    //****** after LPhyParser ******//

    // replace taxa names in TimeTree
    public void replaceTaxaNamesByOrder(TimeTree timeTree) {
        List<String> beastTaxaNm = beastAlignment.getTaxaNames();
        //TODO allow taxa diff ?
        if (beastTaxaNm.size() != timeTree.getTaxaNames().length)
            throw new IllegalArgumentException("The given taxa have to match the taxa in the LPhy model !\n"
                    + beastTaxaNm.size() + " != " + timeTree.getTaxaNames().length);

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

    //    public Alignment getAlignment() {
//        Sequence human = new Sequence("0human", "AAAACCCCGGGGTTTT");
//        Sequence chimp = new Sequence("1chimp", "ACGTACGTACGTACGT");
//        Sequence gorilla = new Sequence("2gorilla", "ATGTACGTACGTACTT");
//
//        Alignment data = new Alignment();
//        data.initByName("sequence", human, "sequence", chimp, "sequence", gorilla,
//                "dataType", "nucleotide"
//        );
//        return data;
//    }

//    public void replaceAlignment(final Set<BEASTInterface> elements, final SortedMap<String, Taxon> allTaxa) {
//        Alignment alg = null;
//        for (BEASTInterface bo : elements) {
//            if (bo instanceof Alignment) {
//                Alignment newAlg = getAlignment();
//                List<Sequence> newSeqs = newAlg.sequenceInput.get();
//                alg = (Alignment) bo;
//
//                // validate SiteCount
//                if (newAlg.getSiteCount() != alg.getSiteCount()) {
//                    System.err.println("Warning: the number of sites ("+ alg.getSiteCount() +
//                            ") defined in LPhy != " + " sites (" + newAlg.getSiteCount() +
//                            ") in the given alignment !");
//                }
//
//                alg.sequenceInput.get().clear();
//                // this will add list, not replace
//                alg.sequenceInput.setValue(newSeqs, alg);
//                alg.initAndValidate();
//
//            }
//        }
//
//        assert alg != null;
//
//        replaceAllTaxaBy(allTaxa, alg); // TODO cannot access taxonset in Tree
//
//    }


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
