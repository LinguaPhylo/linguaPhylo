package lphystudio.core.logger;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class ValueRow {

    public final String title;
    public final DescriptiveStatistics stats;
    public final int row;

    public ValueRow(String title, int row, DescriptiveStatistics stats) {
        this.title = title;
        this.stats = stats;
        this.row = row;
    }
}
