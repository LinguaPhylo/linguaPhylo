package lphybeast.tobeast.data;

import beast.evolution.alignment.Alignment;
import lphy.core.LPhyParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Walter Xie
 */
public class NexusParser extends beast.util.NexusParser {

    // TODO not sure how they can be changed
    // these keywords are fixed inside NexusParser
    public final static String NTAX = "ntax";
    public final static String NCHAR = "nchar";
    public final static String TIP_DATE = "tipcalibration";
    public final static String CHARSET = "charset";


    Map<String, String> keywdVal;


    public NexusParser() {
        super();
    }

    public NexusParser(Path nexfile) {
        super();
        try {
            super.parseFile(nexfile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // must have alignment
        assert m_alignment != null;

        parseRest();
    }


    private void parseRest() {
//            beastParser.traitSet;

        //TODO a lot parsing data to get from nex
//            beastParser.taxa;
//            beastParser.trees;
//            beastParser.calibrations;
//            beastParser.filteredAlignments;

        int ntaxa = getAlignment().getTaxonCount();
        int L = getAlignment().getSiteCount();

        keywdVal = new HashMap<>();
        // real data
        keywdVal.put(NTAX, Integer.toString(ntaxa));
        keywdVal.put(NCHAR, Integer.toString(L));

        keywdVal.put(TIP_DATE, "");

    }

    public Alignment getAlignment() {
        if (m_alignment == null) throw new IllegalArgumentException("Alignment must be available !");
        return m_alignment;
    }

    public String getVal(String nexusVar) {
        return keywdVal.get(nexusVar);
    }

    public String assignArgsTo(LPhyParser parser) {
return "";
    }



}
