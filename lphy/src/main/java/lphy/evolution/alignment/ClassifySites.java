package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.graphicalModel.*;
import lphy.util.LoggerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Walter Xie
 */
public class ClassifySites extends DeterministicFunction<Integer[]> {

    Value<Boolean> returnVarSites;
    Value<Boolean> ignoreUnknownState;
    Value<Alignment> originalAlignment;
    final SequenceType sequenceType;

    public final String isVarSiteParamName = "varSites";
    public final String ignoreUnkParamName = "ignoreUnknown";

    public ClassifySites( @ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME, description = "the original alignment.")
                          Value<Alignment> originalAlignment,
                          @ParameterInfo(name = isVarSiteParamName,
            description = "If true, return the variable sites, otherwise return constant sites.",
            optional=true) Value<Boolean> returnVarSites,
                         @ParameterInfo(name = ignoreUnkParamName,
            description = "If true, ignore the unknown state '?' (incl. gap '-'), when determine variable sites or constant sites.",
            optional=true) Value<Boolean> ignoreUnknownState  ) {
        if (returnVarSites == null)
            returnVarSites = new Value<>(null, Boolean.TRUE);
        this.returnVarSites = returnVarSites;

        if (ignoreUnknownState == null)
            ignoreUnknownState = new Value<>(null, Boolean.TRUE);
        this.ignoreUnknownState = ignoreUnknownState;

        this.originalAlignment = originalAlignment;
        Alignment origAlg = originalAlignment.value();
        if (origAlg == null)
            throw new IllegalArgumentException("Cannot find Alignment ! " + originalAlignment.getId());
        sequenceType = origAlg.getSequenceType();
    }


    @GeneratorInfo(name = "classifySites",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "Return the array of site indices (start from 0), which are either variable sites or constant sites.")
    public Value<Integer[]> apply() {

        Alignment original = originalAlignment.value();
        final boolean retVar = returnVarSites.value();
        final boolean ignUnk = ignoreUnknownState.value();

        List<Integer> selectedSiteIds = new ArrayList<>();
        for (int j = 0; j < original.nchar(); j++) {
            int[] aSite = new int[original.ntaxa()];
            for (int i = 0; i < original.ntaxa(); i++) {
                aSite[i] = original.getState(i, j);
            }
            // var or const
            if (retVar) {
                if (isVarSite(aSite, ignUnk))
                    selectedSiteIds.add(j);
            } else if (!isVarSite(aSite, ignUnk))
                selectedSiteIds.add(j);
        }
        Integer[] ids = selectedSiteIds.toArray(new Integer[0]);
        LoggerUtils.log.info("Extract " + ids.length + (retVar?" variable":" constant") +
                " sites from " + original.nchar() + " sites in alignment " + originalAlignment.getId());

        return new Value<>(null, ids, this);
    }

    private boolean isVarSite(int[] aSite, boolean ignoreUnk) {
        boolean isVarSite = false;
        int first = aSite[0];
        for (int i = 1; i < aSite.length; i++) {
            int state = aSite[i];
            // if ignore ? or -, then make sure first is not ? or -
            if ( ignoreUnk && (first == sequenceType.getUnknownState().getIndex() ||
                    first == sequenceType.getGapState().getIndex()) ) {
                first = state;
                continue; // skip until find 1st valid state
            }

            if (first != state) {
                // depend on if ignore ? or -
                if (!ignoreUnk)
                    return true;
                else if ( (state != sequenceType.getUnknownState().getIndex() &&
                        state != sequenceType.getGapState().getIndex()) )
                    return true;
            }
        }
        return isVarSite;
    }
}
