package james.core.functions;

import james.graphicalModel.*;
import james.graphicalModel.types.DoubleArray2DValue;
import james.graphicalModel.types.MatrixValue;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by adru001 on 2/02/20.
 */
public class JukesCantor extends DeterministicFunction<Double[][]> {

    String paramName;

    public JukesCantor(@ParameterInfo(name = "rate", description = "the rate of the Jukes-Cantor process. Default value is 1.0.", optional=true) Value<Double> rate) {
        paramName = getParamName(0);

        if (rate != null) setParam(paramName, rate);
    }


    @FunctionInfo(name = "jukesCantor", description = "The Jukes-Cantor Q matrix construction function. Takes a mean rate and produces a Jukes-Cantor Q matrix.")
    public Value<Double[][]> apply() {
        Value<Double> rateValue = getParams().get(paramName);
        double rate = (rateValue != null) ? rateValue.value() : 1.0;
        String id = (rateValue != null) ? rateValue.getId() : "";
        return new DoubleArray2DValue(getName() + "(" + id + ")", jc(rate), this);
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
