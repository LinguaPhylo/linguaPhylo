package lphy.evolution.substitutionmodel;

import beast.core.BEASTInterface;
import lphy.beast.BEASTContext;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

import java.util.Map;

/**
 * Created by adru001 on 2/02/20.
 */
public class JukesCantor extends RateMatrix {

    String paramName;

    public JukesCantor(@ParameterInfo(name = "rate", description = "the rate of the Jukes-Cantor process. Default value is 1.0.", optional=true) Value<Double> rate) {
        paramName = getParamName(0);

        if (rate != null) setParam(paramName, rate);
    }


    @GeneratorInfo(name = "jukesCantor", description = "The Jukes-Cantor Q matrix construction function. Takes a mean rate and produces a Jukes-Cantor Q matrix.")
    public Value<Double[][]> apply() {
        Value<Double> rateValue = getParams().get(paramName);
        double rate = (rateValue != null) ? rateValue.value() : 1.0;
        String id = (rateValue != null) ? rateValue.getId() : "";
        return new DoubleArray2DValue( jc(rate), this);
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


    @Override
    public BEASTInterface toBEAST(BEASTInterface value, BEASTContext context) {
        beast.evolution.substitutionmodel.JukesCantor beastJC = new beast.evolution.substitutionmodel.JukesCantor();
        beastJC.initAndValidate();
        return beastJC;
    }
}
