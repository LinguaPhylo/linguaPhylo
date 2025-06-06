package lphy.base.function.alignment;

import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.AlignmentUtils;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.ArrayList;
import java.util.List;

public class InformativeSites extends DeterministicFunction<Integer[]> {

    public InformativeSites(@ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME,
            description = "the original alignment.") Value<Alignment> originalAlignment,
                            @ParameterInfo(name = AlignmentUtils.IGNORE_UNKNOWN_PARAM_NAME,
            description = "If true (as default), ignore the unknown state '?' (incl. gap '-'), when determine parsimony informative sites.",
            optional=true) Value<Boolean> ignoreUnknownState  ) {
        Alignment origAlg = originalAlignment.value();
        if (origAlg == null)
            throw new IllegalArgumentException("Cannot find Alignment ! " + originalAlignment.getId());
        setParam(AlignmentUtils.ALIGNMENT_PARAM_NAME, originalAlignment);

        if (ignoreUnknownState != null)
            setParam(AlignmentUtils.IGNORE_UNKNOWN_PARAM_NAME, ignoreUnknownState);
    }


    @GeneratorInfo(name = "informativeSites",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "Return the array of site indices (start from 0), at the given alignment, which parsimony informative sites.")
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
            // informative sites
            if (AlignmentUtils.isInformative(aSite, ignUnk == null || ignUnk.value(), original.getSequenceType()))
                selectedSiteIds.add(j);
        }
        Integer[] ids = selectedSiteIds.toArray(new Integer[0]);
        LoggerUtils.log.info("Extract " + ids.length + " parsimony informative sites from " +
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
