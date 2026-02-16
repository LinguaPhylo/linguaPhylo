package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * Column-binds two Double[][] matrices into a single matrix.
 * Both matrices must have the same number of rows.
 * For 3+ matrices, chain calls: cbind(cbind(X1, X2), X3).
 */
public class Cbind extends DeterministicFunction<Double[][]> {

    public static final String aParamName = "a";
    public static final String bParamName = "b";

    public Cbind(
            @ParameterInfo(name = aParamName, description = "The first matrix (Double[][]).") Value<Double[][]> a,
            @ParameterInfo(name = bParamName, description = "The second matrix (Double[][]).") Value<Double[][]> b) {

        if (a == null || a.value() == null)
            throw new IllegalArgumentException("Parameter 'a' is null.");
        if (b == null || b.value() == null)
            throw new IllegalArgumentException("Parameter 'b' is null.");

        setParam(aParamName, a);
        setParam(bParamName, b);
    }

    @GeneratorInfo(name = "cbind",
            description = "Column-bind two matrices. Both must have the same number of rows. " +
                    "The result has nRows rows and (aCols + bCols) columns. " +
                    "For combining 3+ matrices, chain calls: cbind(cbind(X1, X2), X3).")
    @Override
    public Value<Double[][]> apply() {
        Double[][] a = ((Value<Double[][]>) getParams().get(aParamName)).value();
        Double[][] b = ((Value<Double[][]>) getParams().get(bParamName)).value();

        if (a.length != b.length) {
            throw new IllegalArgumentException("Row count mismatch: a has " + a.length +
                    " rows but b has " + b.length + " rows.");
        }

        int nRows = a.length;
        int aCols = a[0].length;
        int bCols = b[0].length;
        Double[][] result = new Double[nRows][aCols + bCols];

        for (int r = 0; r < nRows; r++) {
            System.arraycopy(a[r], 0, result[r], 0, aCols);
            System.arraycopy(b[r], 0, result[r], aCols, bCols);
        }

        return new Value<>("", result, this);
    }
}
