package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * lewisMK: for discrete morphological data.
 * @author Alexei Drummond
 */
@Citation(value="Lewis, P. O. (2001). " +
        "A likelihood approach to estimating phylogeny from discrete morphological character data. " +
        "Systematic biology, 50(6), 913-925.",
        title = "A Likelihood Approach to Estimating Phylogeny from Discrete Morphological Character Data",
        year=2001,
        authors={"Lewis"},
        DOI="https://doi.org/10.1080/106351501753462876")
public class LewisMK extends RateMatrix {

    public static final String numStatesParamName = "numStates";

    public LewisMK(@ParameterInfo(name = numStatesParamName, description = "the number of states") Value<Integer> numStates,
                   @ParameterInfo(name = meanRateParamName, description = "the mean rate of the LewisMK process. Default value is 1.0.", optional=true) Value<Number> rate) {

        super(rate);
        setParam(numStatesParamName, numStates);
    }

    @GeneratorInfo(name = "lewisMK", verbClause = "is", narrativeName = "LewisMK model",
            category = GeneratorCategory.Q_MATRIX, examples = {"lewisMKCoalescent.lphy"},
            description = "The LewisMK Q matrix construction function. Takes a mean rate and a number of states and produces a LewisMK Q matrix.")
    public Value<Double[][]> apply() {
        Value<Integer> numStates = getParams().get(numStatesParamName);
        Value<Number> rateValue = getParams().get(meanRateParamName);
        double rate = (rateValue != null) ? doubleValue(rateValue) : 1.0;
        return new DoubleArray2DValue( jc(rate, numStates.value()), this);
    }

    static Double[][] jc(Double meanRate, int numStates) {
        Double[][] Q = new Double[numStates][numStates];

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                if (i != j) {
                    Q[i][j] = 1.0 / (numStates-1.0) * meanRate;
                } else {
                    Q[i][i] = -1.0 * meanRate;
                }
            }
        }
        return Q;
    }

    public Value<Integer> getNumStates() {
        return getParams().get(numStatesParamName);
    }
}
