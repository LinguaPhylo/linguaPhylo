package lphy.base.evolution.substitutionmodel;

import lphy.core.model.GraphicalModelNode;
import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.DoubleArray2DValue;

import java.util.stream.Stream;

/**
 * An empirical amino acid substitution model,
 * derived from {@link jebl.evolution.substmodel.WAG}.
 * Rate matrix is 20 × 20, and frequencies are 20.
 * @author Walter Xie
 */
@Citation(value="Whelan, S., & Goldman, N. (2001). " +
        "A general empirical model of protein evolution derived from multiple protein families using a maximum-likelihood approach. " +
        "Molecular biology and evolution, 18(5), 691-699.",
        title = "A general empirical model of protein evolution derived from multiple protein families using a maximum-likelihood approach",
        year = 2001,
        authors = {"Whelan", "Goldman"},
        DOI="https://doi.org/10.1093/oxfordjournals.molbev.a003851")
public class WAG extends RateMatrix {

    protected final String freqParamName = SubstModelParamNames.FreqParamName;

    public WAG(@ParameterInfo(name = freqParamName, description = "the base frequencies.", optional=true) Value<Double[]> freq,
               @ParameterInfo(name = RateMatrix.meanRateParamName, description = "the mean rate of the process. default 1.0", optional=true) Value<Number> meanRate) {

        super(meanRate);

        if (freq != null) {
            if (freq.value().length != 20)
                throw new IllegalArgumentException("Amino acid frequencies must have 20 dimensions.");

            setParam(freqParamName, freq);
        }
    }

    @GeneratorInfo(name = "wag", verbClause = "is", narrativeName = "WAG model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"wagCoalescent.lphy"},
            description = "The WAG instantaneous rate matrix for amino acid.")
    public Value<Double[][]> apply() {
        Value<Double[]> freq = getParams().get(freqParamName);

        Double[][] Q = freq != null ? getQ(freq.value()) : getQ(null);

        return new DoubleArray2DValue(Q, this);
    }

    protected Double[][] getQ(Double[] freqs) {
        double[] f;
        if (freqs != null) {
            f = Stream.of(freqs).mapToDouble(Double::doubleValue).toArray();
        } else {
            f = jebl.evolution.substmodel.WAG.getOriginalFrequencies();
        }
        jebl.evolution.substmodel.WAG wag = new jebl.evolution.substmodel.WAG(f);
        // this will rebuildRateMatrix(rate,parameters); and incompleteFromQToR();
        double totalRate = wag.setParametersNoScale(null);
        // this is Q before normalise
        double[][] Q = wag.getRelativeRates();

        // normalise rate matrix to one expected substitution per unit time
        return normalize(f, Q, totalRateDefault1());
    }

    // can be null
    public GraphicalModelNode<?> getFreq() {
        return getParams().get(freqParamName);
    }

}
