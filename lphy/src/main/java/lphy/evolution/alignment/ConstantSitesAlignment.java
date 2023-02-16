package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.graphicalModel.*;

import java.util.ArrayList;
import java.util.List;

/**
 * replaced by {@link VariableSites} and {@link CopySites}
 * @author Walter Xie
 */
@Deprecated
public class ConstantSitesAlignment extends DeterministicFunction<Alignment> {

    Value<Alignment> originalAlignment;
    SequenceType sequenceType;
    public final String ignoreUnknownParamName = "ignoreUnknown";

    boolean ignoreUnknown = true;

    public ConstantSitesAlignment(@ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME,
            description = "the unprocessed alignment.")
                                  Value<Alignment> originalAlignment,
                                  @ParameterInfo(name = ignoreUnknownParamName, optional = true,
            description = "ignore unknown states (include gaps) when processing constant sites, default to true.")
                                  Value<Boolean> ignoreUnknown) {
        if (ignoreUnknown.value() != null)
            this.ignoreUnknown = ignoreUnknown.value();
        this.originalAlignment = originalAlignment;
        Alignment origAlg = originalAlignment.value();
        if (origAlg == null)
            throw new IllegalArgumentException("Cannot find Alignment ! " + originalAlignment.getId());
        sequenceType = origAlg.getSequenceType();
    }

    @GeneratorInfo(name = "constantSitesAlignment",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "Keep all constant sites, where the unknown state (include gaps) can be either ignored or not.")
    public Value<Alignment> apply() {

        Alignment original = originalAlignment.value();

        List<int[]> newSites = new ArrayList<>();

        for (int j = 0; j < original.nchar(); j++) {
            int[] aSite = new int[original.ntaxa()];
            for (int i = 0; i < original.ntaxa(); i++) {
                aSite[i] = original.getState(i, j);
            }
            // filter
            if (AlignmentUtils.isInvarSite(aSite, ignoreUnknown, sequenceType))
                newSites.add(aSite);
        }

        // have to know nchar before create a new alignment
        Alignment newAlignment = new SimpleAlignment(newSites.size(), original);
        for (int j = 0; j < newSites.size(); j++) {
            int[] aSite = newSites.get(j);
            for (int i = 0; i < original.ntaxa(); i++) {
                newAlignment.setState(i, j, aSite[i]);
            }
        }

        return new Value<>(null, newAlignment, this);
    }




}
