package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.graphicalModel.*;
import lphy.util.LoggerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Walter Xie
 */
public class VariableSites extends DeterministicFunction<Integer[]> {

    Value<Boolean> ignoreUnknownState;
    Value<Alignment> originalAlignment;
    final SequenceType sequenceType;

    public VariableSites(@ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME,
            description = "the original alignment.") Value<Alignment> originalAlignment,
                         @ParameterInfo(name = AlignmentUtils.IGNORE_UNKNOWN_PARAM_NAME,
            description = "If true (as default), ignore the unknown state '?' (incl. gap '-'), when determine variable sites or constant sites.",
            optional=true) Value<Boolean> ignoreUnknownState  ) {
        if (ignoreUnknownState == null)
            ignoreUnknownState = new Value<>(null, true);
        this.ignoreUnknownState = ignoreUnknownState;

        this.originalAlignment = originalAlignment;
        Alignment origAlg = originalAlignment.value();
        if (origAlg == null)
            throw new IllegalArgumentException("Cannot find Alignment ! " + originalAlignment.getId());
        sequenceType = origAlg.getSequenceType();
    }


    @GeneratorInfo(name = "variableSites",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "Return the array of site indices (start from 0), at the given alignment, " +
                    "which variable sites.")
    public Value<Integer[]> apply() {

        Alignment original = originalAlignment.value();
        final boolean ignUnk = ignoreUnknownState.value();

        List<Integer> selectedSiteIds = new ArrayList<>();
        for (int j = 0; j < original.nchar(); j++) {
            int[] aSite = new int[original.ntaxa()];
            for (int i = 0; i < original.ntaxa(); i++) {
                aSite[i] = original.getState(i, j);
            }
            // var sites
            if (!AlignmentUtils.isInvarSite(aSite, ignUnk, sequenceType))
                selectedSiteIds.add(j);
        }
        Integer[] ids = selectedSiteIds.toArray(new Integer[0]);
        LoggerUtils.log.info("Extract " + ids.length + " variable sites from " +
                original.nchar() + " sites in alignment " + originalAlignment.getId());

        return new Value<>(null, ids, this);
    }
    
}
