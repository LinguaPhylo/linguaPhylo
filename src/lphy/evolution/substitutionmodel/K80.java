package lphy.evolution.substitutionmodel;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy.beast.BEASTContext;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;

import java.util.Map;

/**
 * Created by adru001 on 2/02/20.
 */
public class K80 extends RateMatrix {

    String paramName;

    public K80(@ParameterInfo(name = "kappa", description = "the kappa of the K80 process.") Value<Double> kappa) {
        paramName = getParamName(0);
        setParam(paramName, kappa);
    }


    @GeneratorInfo(name = "k80", description = "The K80 instantaneous rate matrix. Takes a kappa and produces a K80 rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double> kappa = getKappa();
        return new DoubleArray2DValue(k80(kappa.value()), this);
    }

    private Double[][] k80(double kappa) {

        int numStates = 4;

        Double[][] Q = new Double[numStates][numStates];

        double[] totalRates = new double[numStates];

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                if (i != j) {
                    if (Math.abs(i-j) == 2) {
                        Q[i][j] = kappa;
                    } else {
                        Q[i][j] = 1.0;
                    }
                }
                totalRates[i] += Q[i][j];
            }
            Q[i][i] = -totalRates[i];
        }

        normalize(new Double[] {0.25, 0.25, 0.25, 0.25}, Q);

        return Q;
    }

    public Value<Double> getKappa() {
        return getParams().get(paramName);
    }

    public BEASTInterface toBEAST(BEASTInterface value, BEASTContext context) {
        beast.evolution.substitutionmodel.HKY beastHKY = new beast.evolution.substitutionmodel.HKY();
        beastHKY.setInputValue("kappa", context.getBEASTObject(getKappa()));
        beastHKY.setInputValue("frequencies", BEASTContext.createBEASTFrequencies(BEASTContext.createRealParameter(new Double[] {0.25, 0.25, 0.25, 0.25})));
        beastHKY.initAndValidate();
        return beastHKY;
    }
}
