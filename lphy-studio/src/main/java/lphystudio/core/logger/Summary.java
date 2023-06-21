package lphystudio.core.logger;

import java.util.List;

public class Summary {

    public double[] mean;
    public double[] stdev;
    public double[] stderr;
    public double[] min;
    public double[] max;

    public final boolean isLengthSummary;

    public Summary(List<Double[]> values) {
        boolean allSameLength = allSameLength(values);

        if (allSameLength) {
            int length = values.get(0).length;

            mean = new double[length];
            stdev = new double[length];
            stderr = new double[length];
            min = new double[length];
            max = new double[length];

            for (int i = 0; i < length; i++) {
                min[i] = Double.POSITIVE_INFINITY;
                max[i] = Double.NEGATIVE_INFINITY;
            }

            for (Double[] val : values) {
                for (int i = 0; i < length; i++) {
                    mean[i] += val[i];
                    if (val[i] < min[i]) min[i] = val[i];
                    if (val[i] > max[i]) max[i] = val[i];
                }
            }
            for (int i = 0; i < length; i++) {
                mean[i] /= values.size();
            }

            for (Double[] val : values) {
                for (int i = 0; i < length; i++) {
                    stdev[i] += (val[i] - mean[i]) * (val[i] - mean[i]);
                }
            }
            for (int i = 0; i < length; i++) {
                stdev[i] = Math.sqrt(stdev[i] / values.size());
                stderr[i] = stdev[i] / Math.sqrt(values.size());
            }
        } else {
            mean = new double[1];
            stdev = new double[1];
            stderr = new double[1];

            for (Double[] doubles : values) {
                mean[0] += doubles.length;
            }
            mean[0] /= values.size();

            for (Double[] doubles : values) {
                double val = doubles.length;
                stdev[0] += (val - mean[0]) * (val - mean[0]);
            }
            stdev[0] = Math.sqrt(stdev[0] / values.size());
            stderr[0] = stdev[0] / Math.sqrt(values.size());
        }

        isLengthSummary = !allSameLength;
    }

    public int getRowCount() {
        return mean.length;
    }

    public Double[] getRowSummary(int row) {
        return new Double[]{mean[row], stdev[row], stderr[row]};
    }

    public static boolean allSameLength(List<Double[]> values) {
        int length = values.get(0).length;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i).length != length) return false;
        }
        return true;
    }
}
