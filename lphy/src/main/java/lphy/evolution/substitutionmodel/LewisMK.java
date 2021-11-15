package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArray2DValue;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
public class LewisMK extends RateMatrix {

    public static final String numStatesParamName = "numStates";

    public LewisMK(@ParameterInfo(name = numStatesParamName, description = "the number of states") Value<Integer> numStates,
                   @ParameterInfo(name = meanRateParamName, description = "the mean rate of the LewisMK process. Default value is 1.0.", optional=true) Value<Number> rate) {

        super(rate);
        setParam(numStatesParamName, numStates);
    }

    @GeneratorInfo(name = "lewisMK", description = "The LewisMK Q matrix construction function. Takes a mean rate and a number of states and produces a LewisMK Q matrix.")
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
