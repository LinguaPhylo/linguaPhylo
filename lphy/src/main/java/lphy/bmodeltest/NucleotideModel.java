package lphy.bmodeltest;

import lphy.evolution.substitutionmodel.RateMatrix;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

import java.util.Map;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
@Citation(
        value = "Bouckaert, R., Drummond, A. bModelTest: Bayesian phylogenetic site model averaging and model comparison. BMC Evol Biol 17, 42 (2017). https://doi.org/10.1186/s12862-017-0890-6",
        title = "bModelTest: Bayesian phylogenetic site model averaging and model comparison",
        authors = {"Bouckaert", "Drummond"},
        year = 2017,
        DOI = "https://doi.org/10.1186/s12862-017-0890-6"
)
public class NucleotideModel extends RateMatrix {

    public static final String ratesParamName = "rates";
    public static final String freqParamName = "freq";
    public static final String modelParamName = "modelIndicator";
    public static final String modelSetParamName = "modelSet";

    public NucleotideModel(
            @ParameterInfo(name = modelSetParamName, narrativeName = "model set", description = "The set of models to choose from. Valid value are: allreversible, transitionTransversionSplit, namedSimple, namedExtended.") Value<BModelSet> modelSet,
            @ParameterInfo(name = modelParamName, narrativeName = "model index", description = "the index of the model to be employed") Value<Integer> modelIndex,
            @ParameterInfo(name = ratesParamName, narrativeName = "relative rates", description = "the relative rates of the GTR process.") Value<Double[]> rates,
            @ParameterInfo(name = freqParamName, narrativeName = "base frequencies", description = "the base frequencies.") Value<Double[]> freq,
            @ParameterInfo(name = meanRateParamName, narrativeName = "substitution rate", description = "the rate of substitution.", optional = true) Value<Number> meanRate) {

        super(meanRate);

        if (rates.value().length != 6) throw new IllegalArgumentException("Rates must have 6 dimensions.");

        setParam(ratesParamName, rates);
        setParam(freqParamName, freq);
        setParam(modelParamName, modelIndex);
        setParam(modelSetParamName, modelSet);
    }

    @GeneratorInfo(name = "nucleotideModel", verbClause = "is", narrativeName = "bModelTest rate matrix",
            category = GeneratorCategory.RATE_MATRIX, examples = {"simpleBModelTest.lphy","simpleBModelTest2.lphy"},
            description = "The instantaneous rate matrix. Takes relative rates and base frequencies and produces an GTR rate matrix.")
    public Value<Double[][]> apply() {
        Map<String, Value> params = getParams();
        Value<Double[]> rates = getRates();
        Value<Double[]> freq = params.get(freqParamName);
        Value<Integer> modelIndex = params.get(modelParamName);
        Value<BModelSet> modelSet = params.get(modelSetParamName);
        return new DoubleArray2DValue(bModelTest(modelSet.value(), modelIndex.value(), rates.value(), freq.value()), this);
    }


    private Double[][] bModelTest(BModelSet modelSet, int modelIndex, Double[] rawRates, Double[] freqs) {

        int numStates = 4;

        Double[][] Q = new Double[numStates][numStates];

        int [] model = modelSet.getModel(modelIndex);

        double[] relativeRates = new double[rawRates.length];
        for (int i =0; i < relativeRates.length; i++) {
            relativeRates[i] = rawRates[model[i]];
        }

        // construct off-diagonals
        int upper = 0;
        for (int i = 0; i < numStates; i++) {
            for (int j = i + 1; j < numStates; j++) {
                Q[i][j] = relativeRates[upper] * freqs[j];
                Q[j][i] = relativeRates[upper] * freqs[i];
                upper += 1;
            }
        }

        // construct diagonals
        for (int i = 0; i < numStates; i++) {
            double totalRate = 0.0;
            for (int j = 0; j < numStates; j++) {
                if (j != i) {
                    totalRate += Q[i][j];
                }
            }
            Q[i][i] = -totalRate;
        }
        // normalise rate matrix to one expected substitution per unit time
        normalize(freqs, Q, totalRateDefault1());

        return Q;
    }

    public Value<Double[]> getRates() {
        return getParams().get(ratesParamName);
    }

    public Value<Double[]> getFreq() {
        return getParams().get(freqParamName);
    }
}
