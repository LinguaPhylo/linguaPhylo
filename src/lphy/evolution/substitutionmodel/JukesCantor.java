package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Created by adru001 on 2/02/20.
 */
@Citation(value="Jukes, T. H., & Cantor, C. R. (1969). Evolution of protein molecules. Mammalian protein metabolism, 3, 21-132.",
        year=1969,
        authors={"Jukes", "Cantor"},
        DOI="https://doi.org/10.1016/B978-1-4832-3211-9.50009-7")
public class JukesCantor extends RateMatrix {

    public JukesCantor(@ParameterInfo(name = meanRateParamName, description = "the rate of the Jukes-Cantor process. Default value is 1.0.", optional = true) Value<Number> rate) {
        super(rate);
    }

    @GeneratorInfo(name = "jukesCantor",
            verbClause = "is",
            narrativeName = "Jukes-Cantor model",
            description = "The Jukes-Cantor Q matrix construction function. Takes a mean rate and produces a Jukes-Cantor Q matrix.")
    public Value<Double[][]> apply() {
        Value<Number> rateValue = getParams().get(meanRateParamName);
        double rate = (rateValue != null) ? doubleValue(rateValue) : 1.0;
        return new DoubleArray2DValue(LewisMK.jc(rate, 4), this);
    }
}
