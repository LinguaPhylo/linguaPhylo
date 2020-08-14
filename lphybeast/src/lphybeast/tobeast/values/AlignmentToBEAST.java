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

        if (dataExchanger == null) {
            List<Sequence> sequences = new ArrayList<>();

            String[] taxaNames = alignment.getTaxaNames();

            for (int i = 0; i < alignment.getTaxonCount(); i++) {
                context.addTaxon(taxaNames[i]);
                sequences.add(createBEASTSequence(taxaNames[i], alignment.getSequence(i)));
            }

            beast.evolution.alignment.Alignment beastAlignment = new beast.evolution.alignment.Alignment();
            beastAlignment.setInputValue("sequence", sequences);
            beastAlignment.initAndValidate();

            if (!alignmentValue.isAnonymous()) beastAlignment.setID(alignmentValue.getCanonicalId());

            return beastAlignment;
        } else {
            // validation and map taxa
            beast.evolution.alignment.Alignment beastAlignment = dataExchanger.getAlignment();

            // TODO allow diff
            assert alignment.getTaxonCount() == beastAlignment.getTaxonCount();

            return beastAlignment;
        }
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
