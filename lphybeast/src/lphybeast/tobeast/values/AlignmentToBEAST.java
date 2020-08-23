package lphybeast.tobeast.values;

import beast.evolution.alignment.Sequence;
import lphy.evolution.alignment.Alignment;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;
import lphybeast.tobeast.data.DataExchanger;

import java.util.ArrayList;
import java.util.List;

public class AlignmentToBEAST implements ValueToBEAST<Alignment, beast.evolution.alignment.Alignment> {

    protected DataExchanger dataExchanger;

    /**
     * Call this to use simulated alignment {@link Alignment}.
     */
    public AlignmentToBEAST() { }

    /**
     * Call this to use given (real data) alignment.
     * @param dataExchanger using {@link beast.evolution.alignment.Alignment} (real data)
     */
    public AlignmentToBEAST(DataExchanger dataExchanger) { this.dataExchanger = dataExchanger; }

    @Override
    public beast.evolution.alignment.Alignment valueToBEAST(Value<Alignment> alignmentValue, BEASTContext context) {

        Alignment alignment = alignmentValue.value();
        String[] taxaNames = alignment.getTaxaNames();
        beast.evolution.alignment.Alignment beastAlignment;

        if (dataExchanger == null) {
            List<Sequence> sequences = new ArrayList<>();

            for (int i = 0; i < taxaNames.length; i++) {
                context.addTaxon(taxaNames[i]);
                sequences.add(createBEASTSequence(taxaNames[i], alignment.getSequence(i)));
            }

            beastAlignment = new beast.evolution.alignment.Alignment();
            beastAlignment.setInputValue("sequence", sequences);
            beastAlignment.initAndValidate();


        } else {
            String algID = alignmentValue.getCanonicalId();
            assert algID != null;

            // validation and map taxa
//            beastAlignment = dataExchanger.getBEASTAlignment(algID);
            beastAlignment = dataExchanger.getAlignment();
            // TODO allow diff
//            assert alignment.getTaxonCount() == beastAlignment.getTaxonCount();
            if (beastAlignment.getTaxonCount() != taxaNames.length)
                throw new IllegalArgumentException("The given taxa have to match the taxa in the LPhy model !\n"
                        + beastAlignment.getTaxonCount() + " != " + taxaNames.length);

        }
        // using LPhy var as ID allows multiple alignments
        if (!alignmentValue.isAnonymous()) beastAlignment.setID(alignmentValue.getCanonicalId());
        return beastAlignment;
    }

    private Sequence createBEASTSequence(String taxon, String sequence) {
        Sequence seq = new Sequence();
        seq.setInputValue("taxon", taxon);
        seq.setInputValue("value", sequence);
        seq.initAndValidate();
        return seq;
    }

    @Override
    public Class getValueClass() {
        return Alignment.class;
    }

    @Override
    public Class<beast.evolution.alignment.Alignment> getBEASTClass() {
        return beast.evolution.alignment.Alignment.class;
    }
}
