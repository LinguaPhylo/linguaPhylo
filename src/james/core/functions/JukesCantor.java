package james.core.functions;

import james.graphicalModel.*;

/**
 * Created by adru001 on 2/02/20.
 */
public class JukesCantor extends DeterministicFunction<Double[][]> {

    String paramName;

    public JukesCantor(@ParameterInfo(name = "rate", description = "the rate of the Jukes-Cantor process.") Value<Double> rate) {
        paramName = getParamName(0);
        setParam(paramName, rate);
    }


    @FunctionInfo(name = "jc", description = "The Jukes-Cantor Q matrix construction function. Takes a mean rate and produces a Jukes-Cantor Q matrix.")
    public Value<Double[][]> apply() {
        Value<Double> rate = getParams().get(paramName);
        return new MatrixValue(getName() + "(" + rate.getId() + ")", jc(rate.value()), this);
    }

    private Double[][] jc(Double meanRate) {
        Double[][] Q = new Double[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    Q[i][j] = 1.0 / 3.0 * meanRate;
                } else {
                    Q[i][i] = -1.0 * meanRate;
                }
            }
        }
        return Q;
    }
}
