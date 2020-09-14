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

    String freqParamName;

    public WAG(@ParameterInfo(name = "freq", description = "the base frequencies.", optional=true) Value<Double[]> freq) {
        freqParamName = getParamName(0);

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
        // this triggers private WAG.handleRebuild() TODO make it public
        wag.setDistance(0);
        double[][] rates = wag.getRelativeRates();

        Double[][] Q = new Double[20][20];
        for (int i = 0; i < Q.length; i++) {
            for (int j = 0; j < Q[0].length; j++) {
               Q[i][j] = rates[i][j];
            }
        }
        return Q;
    }

    // can be null
    public GraphicalModelNode<?> getFreq() {
        return getParams().get(freqParamName);
    }

}
