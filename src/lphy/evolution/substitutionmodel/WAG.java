package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.GraphicalModelNode;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArray2DValue;

import java.util.stream.Stream;

/**
 * overwrite {@link jebl.evolution.substmodel.WAG}.
 * @author Walter Xie
 */
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
        // double[][] rate in WAG (jebl) is the symmetric matrix S.
        // fromQToR() should be fromSToQ, where Q={s_ij}x{pi_j}
        // this triggers private WAG.rebuildRateMatrix() and incompleteFromQToR() TODO make them public
        // no normalise
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
