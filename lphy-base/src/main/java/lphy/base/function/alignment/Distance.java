package lphy.base.function.alignment;

import lphy.base.evolution.alignment.Alignment;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import static lphy.base.evolution.alignment.AlignmentUtils.ALIGNMENT_PARAM_NAME;

/**
 * @author Walter Xie
 */
public class Distance extends DeterministicFunction<Double[][]> {

    protected final static String MODEL = "m"; // model is reserved to model block

    public Distance(@ParameterInfo(name = ALIGNMENT_PARAM_NAME,
            description = "the alignment (no unambiguous states).") Value<Alignment> originalAlignment,
                    @ParameterInfo(name = Distance.MODEL, optional = true,
            description = "the evolutionary model, such as JC96 or p, default to p (Hamming) distance")
                    Value<String> model) {
        Alignment origAlg = originalAlignment.value();
        if (origAlg == null)
            throw new IllegalArgumentException("Cannot find Alignment ! " + originalAlignment.getId());
        setParam(ALIGNMENT_PARAM_NAME, originalAlignment);
        setParam(Distance.MODEL, model);
    }

    @GeneratorInfo(name = "distance", category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "It computes a matrix of pairwise distances from a given alignment using an evolutionary model. " +
                    "If there is a gap or an ambiguous state, it is treated as different from any canonical states.")
    public Value<Double[][]> apply() {
        final String model = getModel().value();

        final Alignment alignment = getAlignment().value();
        final int nTaxa = alignment.ntaxa();
        Double[][] seqDist = new Double[nTaxa][nTaxa];

        /**
         * 1. assume the index of SequenceType will match to index of Q.
         * 2. Q only considers unambiguous states, so any other int will cause the site is ignored.
         */
        double hDist;
        double p;
        // pairwise dist
        for (int i = 0; i < nTaxa; i++) {
            for (int j = 0; j < nTaxa; j++) {
                // diagonal is 0
                if (i != j) {
                    hDist = hammingDistance(i, j, alignment);
                } else hDist = 0.0; // avoid null
                // observed proportion of differences
                p = hDist / (double) alignment.nchar();
                if (model.equalsIgnoreCase("p") || model.equalsIgnoreCase("Hamming"))
                    seqDist[i][j] = p;
                else if (model.equalsIgnoreCase("JC96"))
                    if (p == 0) seqDist[i][j] = 0.0;
                    else seqDist[i][j] = - 0.75 * Math.log(1 - (4 * p / 3) );
                else throw new UnsupportedOperationException("Only p or JC96 are available !");
            }
        }

        return new Value<>(null, seqDist, this);
    }

    // If there is a gap or an ambiguous state, it is treated as different from any canonical states.
    private static int hammingDistance(int taxon1, int taxon2, Alignment alignment) {
//        int ignoredSites = 0;
        int hdist = 0;
        for (int s = 0; s < alignment.nchar(); s++) {
            int state1 = alignment.getState(taxon1, s);
            int state2 = alignment.getState(taxon2, s);
            // different
            if (state1 != state2)
                hdist += 1;
//            else
//                ignoredSites ++;
        }
//        LoggerUtils.log.fine("Compute pairwise distances for taxon " + taxon1 + " and " + taxon2 +
//                ", where " + ignoredSites + " sites are ignored.");
        return hdist;
    }

    public Value<Alignment> getAlignment() {
        return getParams().get(ALIGNMENT_PARAM_NAME);
    }

    public Value<String> getModel() {
        Value<String> model = getParams().get(MODEL);
        if (model == null || model.value() == null) {
            model = new Value<>(null, "p");
        }
        return model;
    }
}
