package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by adru001 on 2/02/20.
 */
public class JukesCantor extends RateMatrix {

    public static final String rateParamName = "rate";

    public JukesCantor(@ParameterInfo(name = rateParamName, description = "the rate of the Jukes-Cantor process. Default value is 1.0.", optional = true) Value<Number> rate) {
        if (rate != null) setParam(rateParamName, rate);
    }

    @GeneratorInfo(name = "jukesCantor", description = "The Jukes-Cantor Q matrix construction function. Takes a mean rate and produces a Jukes-Cantor Q matrix.")
    public Value<Double[][]> apply() {
        Value<Number> rateValue = getParams().get(rateParamName);
        double rate = (rateValue != null) ? doubleValue(rateValue) : 1.0;
        return new DoubleArray2DValue(LewisMK.jc(rate, 4), this);
    }
}
