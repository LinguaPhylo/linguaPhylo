package lphy.beast.tobeast;

import beast.core.BEASTInterface;
import beast.evolution.alignment.Sequence;
import lphy.beast.ValueToBEAST;
import lphy.evolution.alignment.Alignment;
import lphy.graphicalModel.GraphicalModelNode;
import lphy.graphicalModel.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlignmentToBEAST implements ValueToBEAST<Alignment> {

    @Override
    public BEASTInterface valueToBEAST(Value<Alignment> alignmentValue, Map<GraphicalModelNode, BEASTInterface> beastObjects) {

        List<Sequence> sequences = new ArrayList<>();

        Alignment alignment = alignmentValue.value();

        String[] taxaNames = alignment.getTaxaNames();

        for (int i = 0; i < alignment.getTaxonCount(); i++) {
            sequences.add(createBEASTSequence(taxaNames[i], alignment.getSequence(i)));
        }

        beast.evolution.alignment.Alignment beastAlignment = new beast.evolution.alignment.Alignment();
        beastAlignment.setInputValue("sequence", sequences);
        beastAlignment.initAndValidate();

        beastAlignment.setID(alignmentValue.getCanonicalId());

        return beastAlignment;
    }

    private Sequence createBEASTSequence(String taxon, String sequence) {
        Sequence seq = new Sequence();
        seq.setInputValue("taxon", taxon);
        seq.setInputValue("value", sequence);
        seq.initAndValidate();
        return seq;
    }

    public Class getValueClass() {
        return Alignment.class;
    }
}
