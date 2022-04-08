package lphystudio.core.log;

public class ValueRow {

    public final String title;
    public final Summary summary;
    public final int row;

    public ValueRow(String title, Summary summary, int row) {
        this.title = title;
        this.summary = summary;
        this.row = row;
    }
}
