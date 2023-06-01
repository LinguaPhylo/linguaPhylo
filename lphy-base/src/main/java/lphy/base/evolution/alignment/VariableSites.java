package lphy.base.evolution.alignment;

import lphy.core.model.components.*;
import lphy.core.util.LoggerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Walter Xie
 */
public class VariableSites extends DeterministicFunction<Integer[]> {

    public VariableSites(@ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME,
            description = "the original alignment.") Value<Alignment> originalAlignment,
                         @ParameterInfo(name = AlignmentUtils.IGNORE_UNKNOWN_PARAM_NAME,
            description = "If true (as default), ignore the unknown state '?' (incl. gap '-'), when determine variable sites or constant sites.",
            optional=true) Value<Boolean> ignoreUnknownState  ) {
        Alignment origAlg = originalAlignment.value();
        if (origAlg == null)
            throw new IllegalArgumentException("Cannot find Alignment ! " + originalAlignment.getId());
        setParam(AlignmentUtils.ALIGNMENT_PARAM_NAME, originalAlignment);

        if (ignoreUnknownState != null)
            setParam(AlignmentUtils.IGNORE_UNKNOWN_PARAM_NAME, ignoreUnknownState);
    }


    @GeneratorInfo(name = "variableSites",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "Return the array of site indices (start from 0), at the given alignment, " +
                    "which variable sites.")
    public Value<Integer[]> apply() {

        Value<Alignment> originalAlignment = getAlignment();
        final Alignment original = originalAlignment.value();
        Value<Boolean> ignUnk = getIgnoreUnknown();

        List<Integer> selectedSiteIds = new ArrayList<>();
        for (int j = 0; j < original.nchar(); j++) {
            int[] aSite = new int[original.ntaxa()];
            for (int i = 0; i < original.ntaxa(); i++) {
                aSite[i] = original.getState(i, j);
            }
            // var sites
            if (!AlignmentUtils.isInvarSite(aSite, ignUnk == null || ignUnk.value(), original.getSequenceType()))
                selectedSiteIds.add(j);
        }
        Integer[] ids = selectedSiteIds.toArray(new Integer[0]);
        LoggerUtils.log.info("Extract " + ids.length + " variable sites from " +
                original.nchar() + " sites in alignment " + originalAlignment.getId());

        return new Value<>(null, ids, this);
    }

    public Value<Alignment> getAlignment() {
        return getParams().get(AlignmentUtils.ALIGNMENT_PARAM_NAME);
    }

    public Value<Boolean> getIgnoreUnknown() {
        return getParams().get(AlignmentUtils.IGNORE_UNKNOWN_PARAM_NAME);
    }
}
