package lphy.base.function.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.alignment.Alignment;
import lphy.core.exception.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.ArrayList;
import java.util.List;

import static lphy.base.evolution.alignment.AlignmentUtils.ALIGNMENT_PARAM_NAME;

/**
 * @author Walter Xie
 */
public class SelectSitesByMissingFraction extends DeterministicFunction<Integer[]> {

    public final String thresholdParamName = "unknownFracLess";

    public SelectSitesByMissingFraction(@ParameterInfo(name = thresholdParamName,
            description = "the threshold to select a site, if the fraction of unknown states (incl. gap) " +
                    "in this site is less than the threshold.")
                                        Value<Double> fracLessThan,
                                        @ParameterInfo(name = ALIGNMENT_PARAM_NAME,
            description = "the original alignment.") Value<Alignment> originalAlignment) {

        if (fracLessThan.value() >= 1)
            throw new IllegalArgumentException("Fraction threshold must < 1 : " + fracLessThan.value());
        setParam(thresholdParamName, fracLessThan);

        Alignment origAlg = originalAlignment.value();
        if (origAlg == null)
            throw new IllegalArgumentException("Cannot find Alignment ! " + originalAlignment.getId());
        setParam(ALIGNMENT_PARAM_NAME, originalAlignment);
    }

    @GeneratorInfo(name = "selectSites",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "Select the site where the fraction of unknown states are less than the threshold in that site.")
    public Value<Integer[]> apply() {

        Value<Alignment> originalAlignment = getAlignment();
        final Alignment original = originalAlignment.value();
        Value<Double> fracLessThan = getUnknownFracLess();

        List<Integer> selectedSiteIds = new ArrayList<>();
        for (int j = 0; j < original.nchar(); j++) {
            int[] aSite = new int[original.ntaxa()];
            for (int i = 0; i < original.ntaxa(); i++) {
                aSite[i] = original.getState(i, j);
            }
            // filter
            if (isSelected(aSite, fracLessThan.value(), original.getSequenceType()))
                selectedSiteIds.add(j);
        }

        Integer[] ids = selectedSiteIds.toArray(new Integer[0]);
        LoggerUtils.log.info("Select " + ids.length + " sites from " +
                original.nchar() + " sites in alignment " + originalAlignment.getId() +
                ", where the fraction of unknown states < " + fracLessThan.value());

        return new Value<>(null, ids, this);
    }

    // select a site, if the fraction of unknown states (incl. gap) < threshold
    private boolean isSelected(int[] aSite, double threshold, SequenceType sequenceType) {
        double missing = 0.0;
        for (int state : aSite) {
            if ( state == sequenceType.getUnknownState().getIndex() ||
                    state == sequenceType.getGapState().getIndex())
                missing++;
        }
        return missing/(double)aSite.length < threshold;
    }

    public Value<Alignment> getAlignment() {
        return getParams().get(ALIGNMENT_PARAM_NAME);
    }

    public Value<Double> getUnknownFracLess() {
        return getParams().get(thresholdParamName);
    }
}
