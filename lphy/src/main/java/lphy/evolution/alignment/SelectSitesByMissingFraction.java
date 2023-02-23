package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.graphicalModel.*;
import lphy.util.LoggerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Walter Xie
 */
public class SelectSitesByMissingFraction extends DeterministicFunction<Integer[]> {

    Value<Double> fracLessThan;
    Value<Alignment> originalAlignment;
    SequenceType sequenceType;
    public final String thresholdParamName = "unknownFracLess";

    public SelectSitesByMissingFraction(@ParameterInfo(name = thresholdParamName,
            description = "the threshold to select a site, if the fraction of unknown states (incl. gap) " +
                    "in this site is less than the threshold.")
                              Value<Double> fracLessThan,
                                        @ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME,
            description = "the original alignment.") Value<Alignment> originalAlignment) {

        this.fracLessThan = fracLessThan;
        if (fracLessThan.value() >= 1)
            throw new IllegalArgumentException("Fraction threshold must < 1 : " + fracLessThan.value());
        this.originalAlignment = originalAlignment;
        Alignment origAlg = originalAlignment.value();
        if (origAlg == null)
            throw new IllegalArgumentException("Cannot find Alignment ! " + originalAlignment.getId());
        sequenceType = origAlg.getSequenceType();
    }

    @GeneratorInfo(name = "selectSites",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "Select the site where the fraction of unknown states are less than the threshold in that site.")
    public Value<Integer[]> apply() {

        Alignment original = originalAlignment.value();

        List<Integer> selectedSiteIds = new ArrayList<>();

        for (int j = 0; j < original.nchar(); j++) {
            int[] aSite = new int[original.ntaxa()];
            for (int i = 0; i < original.ntaxa(); i++) {
                aSite[i] = original.getState(i, j);
            }
            // filter
            if (isSelected(aSite, fracLessThan.value()))
                selectedSiteIds.add(j);
        }

        Integer[] ids = selectedSiteIds.toArray(new Integer[0]);
        LoggerUtils.log.info("Select " + ids.length + " sites from " +
                original.nchar() + " sites in alignment " + originalAlignment.getId() +
                ", where the fraction of unknown states < " + fracLessThan.value());

        return new Value<>(null, ids, this);
    }

    // select a site, if the fraction of unknown states (incl. gap) < threshold
    private boolean isSelected(int[] aSite, double threshold) {
        double missing = 0.0;
        for (int state : aSite) {
            if ( state == sequenceType.getUnknownState().getIndex() ||
                    state == sequenceType.getGapState().getIndex())
                missing++;
        }
        return missing/(double)aSite.length < threshold;
    }
}
