package lphy.base.function.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.AlignmentUtils;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static lphy.base.evolution.alignment.AlignmentUtils.ALIGNMENT_PARAM_NAME;

public class RemoveTaxa extends DeterministicFunction<Alignment> {

    public static final String taxaParamName = "names";

    public RemoveTaxa(@ParameterInfo(name = taxaParamName,
            description = "an array of objects representing taxa names") Value<Object[]> taxaNames,
                      @ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME,
                              description = "the original alignment.") Value<Alignment> originalAlignment) {
        setParam(taxaParamName, taxaNames);
        setParam(ALIGNMENT_PARAM_NAME, originalAlignment);
    }

    @GeneratorInfo(name="rmTaxa", category = GeneratorCategory.TAXA_ALIGNMENT,
            examples = {"jcCoalescent.lphy"}, description = "Remove a set of taxa from the given alignment.")
    public Value<Alignment> apply() {
        Value<Object[]> namesVal = getParams().get(taxaParamName);
        Value<Alignment> alignmentVal = getParams().get(ALIGNMENT_PARAM_NAME);

        List<String> unwantedNameList = Arrays.stream(namesVal.value())
                .map(Object::toString).toList();

        final Alignment original = alignmentVal.value();
        List<Taxon> newTaxonList = new ArrayList<>();
        for (Taxon t : original.getTaxonArray()) {
            // rm all taxon if it is in
            if (!unwantedNameList.contains(t.getName()))
                newTaxonList.add(t);
        }
        Taxa newTaxa = Taxa.createTaxa(newTaxonList.toArray(Taxon[]::new));
        int nchar = original.nchar();
        SequenceType sequenceType = original.getSequenceType();
        Alignment newAlignment = new SimpleAlignment(newTaxa, nchar, sequenceType);

        int tmpS;
        // set states
        for (int i = 0; i < newAlignment.ntaxa(); i++) {
            String name = newAlignment.getTaxonName(i);
            int originalIdx = original.indexOfTaxon(name);
            if (originalIdx < 0)
                throw new IllegalArgumentException("Cannot find the taxon " + name + " in the given alignment : " +
                        Arrays.toString(original.getTaxaNames()));

            for (int j = 0; j < original.nchar(); j++) {
                // original taxon index
                tmpS = original.getState(originalIdx, j);
                // new taxon index
                newAlignment.setState(i, j, tmpS);
            }
        }

        return new Value<>(null, newAlignment, this);
    }
}
