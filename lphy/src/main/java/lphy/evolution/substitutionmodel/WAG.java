package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

import java.util.stream.Stream;

/**
 * Issue https://github.com/LinguaPhylo/linguaPhylo/issues/128
 * overwrite {@link jebl.evolution.substmodel.WAG}.
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

    public static final String freqParamName = "freq";

    public WAG(@ParameterInfo(name = freqParamName, description = "the base frequencies.", optional=true) Value<Double[]> freq,
               @ParameterInfo(name = meanRateParamName, description = "the mean rate of the process. default 1.0", optional=true) Value<Number> meanRate) {

        super(meanRate);

        if (freq != null) {
            if (freq.value().length != 20)
                throw new IllegalArgumentException("Amino acid frequencies must have 20 dimensions.");

            setParam(freqParamName, freq);
        }
    }

    @GeneratorInfo(name = "wag", description = "The WAG instantaneous rate matrix for amino acid.")
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
