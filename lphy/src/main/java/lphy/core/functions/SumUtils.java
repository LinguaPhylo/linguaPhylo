package lphy.core.functions;

public class SumUtils {

    public SumUtils() {

    }

    public Number[] sumRows(Number[][] v) {
        Number[] sum = new Number[v.length];
        for (int i = 0; i < v.length; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < v[i].length; j++) {
                rowSum = rowSum + v[i][j].doubleValue();
            }
            sum[i] = rowSum;
        }
        return sum;
    }

    public Number[] sumCols(Number[][] v) {
        Number[] sum = new Number[v[0].length];
        for (int j = 0; j < v[0].length; j++) {
            double colSum = 0.0;
            for (int i = 0; i < v.length; i++) {
                colSum = colSum + v[i][j].doubleValue();
            }
            sum[j] = colSum;
        }
        return sum;
    }

}
