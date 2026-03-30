package lphy.base.function.alignment;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.AlignmentUtils;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class ExtractAlignment extends DeterministicFunction<Alignment> {
    public static final String taxaName = "taxa";
    public ExtractAlignment(
            @ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME, description = "the alignment that extract from") Value<Alignment> alignment,
            @ParameterInfo(name = taxaName, description = "the string array of taxa that will be extracted") Value<String[]> taxa
    ) {
        if (alignment == null){
            throw new IllegalArgumentException("alignment is null");
        }
        if (taxa == null){
            throw new IllegalArgumentException("taxa is null");
        }
        if (taxa.value().length > alignment.value().ntaxa()){
            throw new IllegalArgumentException("taxa number exceeds alignment taxa number");
        }

        setParam(AlignmentUtils.ALIGNMENT_PARAM_NAME, alignment);
        setParam(taxaName, taxa);
    }

    @GeneratorInfo(name = "extractAlignment", description = "extract several taxa from the alignment")
    @Override
    public Value<Alignment> apply() {
        Alignment alignment = getAlignment().value();
        String[] taxaNames = getTaxa().value();

        // Build Taxon[] for the requested subset, validating each name exists
        Taxon[] taxonSubset = new Taxon[taxaNames.length];
        for (int i = 0; i < taxaNames.length; i++) {
            int srcIndex = alignment.indexOfTaxon(taxaNames[i]);
            if (srcIndex < 0)
                throw new IllegalArgumentException("Taxon '" + taxaNames[i] + "' not found in alignment");
            taxonSubset[i] = alignment.getTaxon(srcIndex);
        }

        Taxa subTaxa = Taxa.createTaxa(taxonSubset);
        Alignment newAlignment = new SimpleAlignment(subTaxa, alignment.nchar(), alignment.getSequenceType());

        // Copy states from the source alignment into the new alignment
        for (int i = 0; i < taxaNames.length; i++) {
            int srcIndex = alignment.indexOfTaxon(taxaNames[i]);
            for (int j = 0; j < alignment.nchar(); j++) {
                newAlignment.setState(i, j, alignment.getState(srcIndex, j));
            }
        }

        return new Value<>(null, newAlignment, this);
    }

    public Value<String[]> getTaxa(){
        return getParams().get(taxaName);
    }

    public Value<Alignment> getAlignment(){
        return getParams().get(AlignmentUtils.ALIGNMENT_PARAM_NAME);
    }
}
