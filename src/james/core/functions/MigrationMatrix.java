package james.core.functions;

import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.FunctionInfo;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.Value;
import james.graphicalModel.types.DoubleArray2DValue;

/**
 * Created by adru001 on 2/02/20.
 */
public class MigrationMatrix extends DeterministicFunction<Double[][]> {

    String thetaParamName;
    String ratesParamName;

    public MigrationMatrix(@ParameterInfo(name = "Theta", description = "the population sizes.") Value<Double[]> popSizes,
                           @ParameterInfo(name = "migrationRates", description = "the migration rates between each pair of demes (row-major order minus diagonals).") Value<Double[]> rates) {
        thetaParamName = getParamName(0);
        ratesParamName = getParamName(1);

        int numPops = popSizes.value().length;
        int numRates = numPops * numPops - numPops;

        if (rates.value().length != numRates)
            throw new IllegalArgumentException("There must be " + numRates + " migration rates for " + numPops + " demes.");

        setParam(thetaParamName, popSizes);
        setParam(ratesParamName, rates);
    }


    @FunctionInfo(name = "migrationMatrix", description = "The population process matrix. Diagonals will be population sizes, off-diagonals will be the migration rate from pop i to pop j (backwards in time in units of expected migrants per generation).")
    public Value<Double[][]> apply() {
        Value<Double[]> rates = getParams().get(ratesParamName);
        Value<Double[]> popSizes = getParams().get(thetaParamName);
        return new DoubleArray2DValue(getName() + "(" + popSizes.getId() + ", " + rates.getId() + ")", migrationMatrix(popSizes.value(), rates.value()), this);
    }

    private Double[][] migrationMatrix(Double[] popSizes, Double[] rates) {

        int numDemes = popSizes.length;

        Double[][] matrix = new Double[numDemes][numDemes];

        // construct matrix
        int index = 0;
        for (int i = 0; i < numDemes; i++) {
            for (int j = 0; j < numDemes; j++) {
                if (i != j) {
                    matrix[i][j] = rates[index];
                    index += 1;
                } else {
                    matrix[i][i] = popSizes[i];
                }
            }
        }
        return matrix;
    }
}
