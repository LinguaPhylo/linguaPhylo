package lphy.base.function.io;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.Table;

import java.util.*;

/**
 * Converts a Table (from readDelim) into a Double[][] design matrix
 * for use with generalLinearFunction.
 *
 * <p>Supported CSV formats:
 *
 * <p><b>Long formats</b> (columns named by role):
 * <ul>
 *   <li>Per-deme: columns "deme", "interval", plus predictor columns.
 *       Rows sorted by (interval ascending, deme alphabetical).</li>
 *   <li>Migration (migrationMatrix=true): columns "from", "to", "interval", plus predictor columns.
 *       Rows sorted by (interval ascending, from alphabetical, to alphabetical), self-pairs skipped.</li>
 * </ul>
 *
 * <p><b>MASCOT matrix formats</b> (auto-detected):
 * <ul>
 *   <li>Ne vector (no header, 2 columns): deme_name,value per line. Output: Double[nDemes][1].</li>
 *   <li>Ne matrix (header starts with blank): blank column + interval columns.
 *       Output: Double[nDemes*nIntervals][1], sorted by (interval, deme).</li>
 *   <li>Square migration matrix (header starts with blank, migrationMatrix=true):
 *       blank column + to-deme columns. Output: Double[nDemes*(nDemes-1)][1], self-pairs skipped.</li>
 *   <li>Time-variant migration matrix (header starts with blank, column names contain ":"):
 *       blank column + to_deme:interval columns.
 *       Output: Double[nDemes*(nDemes-1)*nIntervals][1], self-pairs skipped.</li>
 * </ul>
 *
 * <p><b>nIntervals parameter</b>: for static (single-interval) formats like Ne vector or square
 * migration matrix, specifying nIntervals replicates (tiles) the rows across that many intervals,
 * producing a design matrix compatible with time-variant formats. This is required when combining
 * static predictors with time-variant ones via cbind.
 */
public class toDesignMatrix extends DeterministicFunction<Double[][]> {

    private static final String tableName = "table";
    private static final String flagName = "migrationMatrix";
    private static final String nIntervalsName = "nIntervals";

    /**
     * Classifies the detected CSV format.
     */
    private enum FormatType {
        LONG_PER_DEME,
        LONG_MIGRATION,
        MASCOT_NE_VECTOR,
        MASCOT_NE_MATRIX,
        MASCOT_SQUARE_MIGRATION,
        MASCOT_TIME_VARIANT_MIGRATION
    }

    public toDesignMatrix(
            @ParameterInfo(name = tableName, description = "Table read from a CSV file containing predictor data in one of the supported formats (long or MASCOT matrix).") Value<Table> table,
            @ParameterInfo(name = flagName, description = "If true, treat as migration format; if false (default), treat as per-deme/Ne format. Used to disambiguate MASCOT matrix formats.", optional = true) Value<Boolean> flag,
            @ParameterInfo(name = nIntervalsName, description = "Number of time intervals to tile a static predictor across. Only valid for static formats (Ne vector, square migration matrix). The single-interval block is replicated nIntervals times to match time-variant predictors.", optional = true) Value<Integer> nIntervals) {
        if (table == null || table.value().size() == 0)
            throw new IllegalArgumentException("table is null or empty");
        setParam(tableName, table);
        if (flag != null) {
            setParam(flagName, flag);
        }
        if (nIntervals != null) {
            setParam(nIntervalsName, nIntervals);
        }
    }

    @GeneratorInfo(name = "toDesignMatrix", description = "Convert a table from readDelim into a Double[][] design matrix for generalLinearFunction. " +
            "Supports long formats (deme/interval or from/to/interval columns) and MASCOT matrix formats " +
            "(Ne vector, Ne time-variant matrix, square migration matrix, time-variant migration matrix). " +
            "Format is auto-detected from column names. Rows are sorted in canonical order. " +
            "Use nIntervals to tile static predictors across time intervals for use with cbind.")
    @Override
    public Value<Double[][]> apply() {
        Table table = getTable().value();
        boolean isMigration = false;
        if (getFlag() != null) {
            isMigration = getFlag().value();
        }

        String[] columnNames = table.getColumnNames();
        if (columnNames == null || columnNames.length == 0)
            throw new IllegalArgumentException("Table has no columns.");

        FormatType fmt = detectFormat(table, columnNames, isMigration);
        Double[][] result = buildMatrix(fmt, table, columnNames);

        Value<Integer> nIntervalsVal = getNIntervals();
        if (nIntervalsVal != null) {
            result = tileRows(result, nIntervalsVal.value(), fmt);
        }

        return new Value<>("", result, this);
    }

    private FormatType detectFormat(Table table, String[] columnNames, boolean isMigration) {
        // 1. Long per-deme format: has "deme" and "interval" columns
        if (hasColumn(columnNames, "deme") && hasColumn(columnNames, "interval")) {
            return FormatType.LONG_PER_DEME;
        }

        // 2. Long migration format: has "from", "to", and "interval" columns
        if (hasColumn(columnNames, "from") && hasColumn(columnNames, "to") && hasColumn(columnNames, "interval")) {
            return FormatType.LONG_MIGRATION;
        }

        // 3. MASCOT matrix format: first column name is empty/blank (row labels)
        if (columnNames[0].trim().isEmpty()) {
            if (isMigration) {
                // Check if column names (after first) contain ":" => time-variant migration
                boolean hasColon = false;
                for (int i = 1; i < columnNames.length; i++) {
                    if (columnNames[i].contains(":")) {
                        hasColon = true;
                        break;
                    }
                }
                if (hasColon) {
                    return FormatType.MASCOT_TIME_VARIANT_MIGRATION;
                } else {
                    return FormatType.MASCOT_SQUARE_MIGRATION;
                }
            } else {
                return FormatType.MASCOT_NE_MATRIX;
            }
        }

        // 4. Ne vector: exactly 2 columns, first column is non-numeric (deme names)
        if (columnNames.length == 2) {
            return FormatType.MASCOT_NE_VECTOR;
        }

        throw new IllegalArgumentException("Unrecognized table format. Expected one of: " +
                "long per-deme (deme,interval,predictors), long migration (from,to,interval,predictors), " +
                "MASCOT Ne vector (2 columns, no header), MASCOT Ne/migration matrix (first column blank). " +
                "Available columns: " + Arrays.toString(columnNames));
    }

    private Double[][] buildMatrix(FormatType fmt, Table table, String[] columnNames) {
        switch (fmt) {
            case LONG_PER_DEME:
                return buildPerDemeMatrix(table, columnNames);
            case LONG_MIGRATION:
                return buildMigrationMatrix(table, columnNames);
            case MASCOT_NE_VECTOR:
                return buildMascotNeVector(table, columnNames);
            case MASCOT_NE_MATRIX:
                return buildMascotNeMatrix(table, columnNames);
            case MASCOT_SQUARE_MIGRATION:
                return buildMascotSquareMigrationMatrix(table, columnNames);
            case MASCOT_TIME_VARIANT_MIGRATION:
                return buildMascotTimeVariantMigrationMatrix(table, columnNames);
            default:
                throw new IllegalArgumentException("Unknown format type: " + fmt);
        }
    }

    /**
     * Tile a static single-interval predictor block across nIntervals.
     * Only valid for static formats (MASCOT_NE_VECTOR, MASCOT_SQUARE_MIGRATION).
     * The block is repeated nIntervals times, preserving canonical sort order
     * (interval ascending, then within-interval sort).
     */
    private Double[][] tileRows(Double[][] block, int nIntervals, FormatType fmt) {
        if (nIntervals < 1) {
            throw new IllegalArgumentException("nIntervals must be >= 1, got " + nIntervals);
        }

        switch (fmt) {
            case MASCOT_NE_VECTOR:
            case MASCOT_SQUARE_MIGRATION:
                // These are static single-interval formats — tiling is valid
                break;
            case MASCOT_NE_MATRIX:
            case MASCOT_TIME_VARIANT_MIGRATION:
                throw new IllegalArgumentException(
                        "nIntervals cannot be used with time-variant format (" + fmt + "). " +
                        "The data already contains multiple intervals.");
            case LONG_PER_DEME:
            case LONG_MIGRATION:
                throw new IllegalArgumentException(
                        "nIntervals cannot be used with long format (" + fmt + "). " +
                        "Long formats already encode intervals in the data.");
            default:
                throw new IllegalArgumentException("Unknown format type: " + fmt);
        }

        int blockRows = block.length;
        int nCols = block[0].length;
        Double[][] tiled = new Double[blockRows * nIntervals][nCols];

        for (int interval = 0; interval < nIntervals; interval++) {
            for (int r = 0; r < blockRows; r++) {
                int targetRow = interval * blockRows + r;
                System.arraycopy(block[r], 0, tiled[targetRow], 0, nCols);
            }
        }

        return tiled;
    }

    // ---- Existing long-format builders ----

    private Double[][] buildPerDemeMatrix(Table table, String[] columnNames) {
        int demeCol = findColumn(columnNames, "deme");
        int intervalCol = findColumn(columnNames, "interval");

        List<Integer> predictorIndices = new ArrayList<>();
        for (int i = 0; i < columnNames.length; i++) {
            if (i != demeCol && i != intervalCol) {
                predictorIndices.add(i);
            }
        }
        if (predictorIndices.isEmpty()) {
            throw new IllegalArgumentException("No predictor columns found. Table must have columns beyond 'deme' and 'interval'.");
        }

        int nRows = table.getColumn(0).size();

        TreeMap<String, double[]> sortedRows = new TreeMap<>();
        for (int r = 0; r < nRows; r++) {
            int interval = parseIntValue(table.getColumn(intervalCol).get(r), "interval", r);
            String deme = String.valueOf(table.getColumn(demeCol).get(r));

            String key = String.format("%010d|%s", interval, deme);
            if (sortedRows.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate row for deme='" + deme + "', interval=" + interval);
            }

            double[] predictors = new double[predictorIndices.size()];
            for (int p = 0; p < predictorIndices.size(); p++) {
                predictors[p] = parseDoubleValue(table.getColumn(predictorIndices.get(p)).get(r),
                        columnNames[predictorIndices.get(p)], r);
            }
            sortedRows.put(key, predictors);
        }

        Double[][] result = new Double[sortedRows.size()][predictorIndices.size()];
        int i = 0;
        for (double[] predictors : sortedRows.values()) {
            for (int j = 0; j < predictors.length; j++) {
                result[i][j] = predictors[j];
            }
            i++;
        }
        return result;
    }

    private Double[][] buildMigrationMatrix(Table table, String[] columnNames) {
        int fromCol = findColumn(columnNames, "from");
        int toCol = findColumn(columnNames, "to");
        int intervalCol = findColumn(columnNames, "interval");

        List<Integer> predictorIndices = new ArrayList<>();
        for (int i = 0; i < columnNames.length; i++) {
            if (i != fromCol && i != toCol && i != intervalCol) {
                predictorIndices.add(i);
            }
        }
        if (predictorIndices.isEmpty()) {
            throw new IllegalArgumentException("No predictor columns found. Table must have columns beyond 'from', 'to', and 'interval'.");
        }

        int nRows = table.getColumn(0).size();

        TreeMap<String, double[]> sortedRows = new TreeMap<>();
        for (int r = 0; r < nRows; r++) {
            int interval = parseIntValue(table.getColumn(intervalCol).get(r), "interval", r);
            String from = String.valueOf(table.getColumn(fromCol).get(r));
            String to = String.valueOf(table.getColumn(toCol).get(r));

            if (from.equals(to)) continue;

            String key = String.format("%010d|%s|%s", interval, from, to);
            if (sortedRows.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate row for from='" + from + "', to='" + to + "', interval=" + interval);
            }

            double[] predictors = new double[predictorIndices.size()];
            for (int p = 0; p < predictorIndices.size(); p++) {
                predictors[p] = parseDoubleValue(table.getColumn(predictorIndices.get(p)).get(r),
                        columnNames[predictorIndices.get(p)], r);
            }
            sortedRows.put(key, predictors);
        }

        Double[][] result = new Double[sortedRows.size()][predictorIndices.size()];
        int i = 0;
        for (double[] predictors : sortedRows.values()) {
            for (int j = 0; j < predictors.length; j++) {
                result[i][j] = predictors[j];
            }
            i++;
        }
        return result;
    }

    // ---- MASCOT format builders ----

    /**
     * Ne vector format (no header, read with header=false).
     * Table has Column1=deme names, Column2=values.
     * Output: Double[nDemes][1], sorted by deme name alphabetically.
     */
    private Double[][] buildMascotNeVector(Table table, String[] columnNames) {
        List demeCol = table.getColumn(0);
        List valueCol = table.getColumn(1);
        int nRows = demeCol.size();

        TreeMap<String, Double> sortedRows = new TreeMap<>();
        for (int r = 0; r < nRows; r++) {
            String deme = String.valueOf(demeCol.get(r));
            double value = parseDoubleValue(valueCol.get(r), columnNames[1], r);
            if (sortedRows.containsKey(deme)) {
                throw new IllegalArgumentException("Duplicate deme '" + deme + "' in Ne vector.");
            }
            sortedRows.put(deme, value);
        }

        Double[][] result = new Double[sortedRows.size()][1];
        int i = 0;
        for (Double v : sortedRows.values()) {
            result[i][0] = v;
            i++;
        }
        return result;
    }

    /**
     * MASCOT Ne matrix format.
     * First column (name="") has deme names as row labels.
     * Remaining columns named by interval index ("0","1",...).
     * Output: Double[nDemes * nIntervals][1], sorted by (interval ascending, deme alphabetical).
     */
    private Double[][] buildMascotNeMatrix(Table table, String[] columnNames) {
        List rowLabels = table.getColumn(0);
        int nDemes = rowLabels.size();
        int nIntervals = columnNames.length - 1;

        TreeMap<String, Double> sortedRows = new TreeMap<>();
        for (int r = 0; r < nDemes; r++) {
            String deme = String.valueOf(rowLabels.get(r));
            for (int c = 1; c < columnNames.length; c++) {
                int interval;
                try {
                    interval = Integer.parseInt(columnNames[c].trim());
                } catch (NumberFormatException e) {
                    interval = c - 1;
                }
                double value = parseDoubleValue(table.getColumn(c).get(r), columnNames[c], r);
                String key = String.format("%010d|%s", interval, deme);
                sortedRows.put(key, value);
            }
        }

        Double[][] result = new Double[sortedRows.size()][1];
        int i = 0;
        for (Double v : sortedRows.values()) {
            result[i][0] = v;
            i++;
        }
        return result;
    }

    /**
     * MASCOT square migration matrix format.
     * First column (name="") has from-deme names.
     * Remaining column names are to-deme names.
     * Output: Double[nDemes*(nDemes-1)][1], self-pairs skipped,
     * sorted by (from alphabetical, to alphabetical).
     */
    private Double[][] buildMascotSquareMigrationMatrix(Table table, String[] columnNames) {
        List rowLabels = table.getColumn(0);
        int nDemes = rowLabels.size();

        TreeMap<String, Double> sortedRows = new TreeMap<>();
        for (int r = 0; r < nDemes; r++) {
            String from = String.valueOf(rowLabels.get(r));
            for (int c = 1; c < columnNames.length; c++) {
                String to = columnNames[c].trim();
                if (from.equals(to)) continue;
                double value = parseDoubleValue(table.getColumn(c).get(r), columnNames[c], r);
                String key = String.format("%s|%s", from, to);
                sortedRows.put(key, value);
            }
        }

        Double[][] result = new Double[sortedRows.size()][1];
        int i = 0;
        for (Double v : sortedRows.values()) {
            result[i][0] = v;
            i++;
        }
        return result;
    }

    /**
     * MASCOT time-variant migration matrix format.
     * First column (name="") has from-deme names.
     * Remaining column names encode "to_deme:interval".
     * Output: Double[nDemes*(nDemes-1)*nIntervals][1], self-pairs skipped,
     * sorted by (interval ascending, from alphabetical, to alphabetical).
     */
    private Double[][] buildMascotTimeVariantMigrationMatrix(Table table, String[] columnNames) {
        List rowLabels = table.getColumn(0);
        int nDemes = rowLabels.size();

        TreeMap<String, Double> sortedRows = new TreeMap<>();
        for (int r = 0; r < nDemes; r++) {
            String from = String.valueOf(rowLabels.get(r));
            for (int c = 1; c < columnNames.length; c++) {
                String colName = columnNames[c].trim();
                int lastColon = colName.lastIndexOf(':');
                if (lastColon < 0) {
                    throw new IllegalArgumentException("Expected 'to_deme:interval' in column name, got: '" + colName + "'");
                }
                String to = colName.substring(0, lastColon);
                int interval;
                try {
                    interval = Integer.parseInt(colName.substring(lastColon + 1));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Non-integer interval in column name: '" + colName + "'");
                }

                if (from.equals(to)) continue;

                double value = parseDoubleValue(table.getColumn(c).get(r), colName, r);
                String key = String.format("%010d|%s|%s", interval, from, to);
                sortedRows.put(key, value);
            }
        }

        Double[][] result = new Double[sortedRows.size()][1];
        int i = 0;
        for (Double v : sortedRows.values()) {
            result[i][0] = v;
            i++;
        }
        return result;
    }

    // ---- Utility methods ----

    private boolean hasColumn(String[] columnNames, String name) {
        for (String col : columnNames) {
            if (name.equalsIgnoreCase(col.trim())) {
                return true;
            }
        }
        return false;
    }

    private int findColumn(String[] columnNames, String name) {
        for (int i = 0; i < columnNames.length; i++) {
            if (name.equalsIgnoreCase(columnNames[i].trim())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Required column '" + name + "' not found. Available columns: " +
                Arrays.toString(columnNames));
    }

    private double parseDoubleValue(Object value, String columnName, int row) {
        if (value == null) {
            throw new IllegalArgumentException("Null value in column '" + columnName + "' at row " + row);
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Non-numeric value '" + value + "' in column '" + columnName + "' at row " + row);
        }
    }

    private int parseIntValue(Object value, String columnName, int row) {
        if (value == null) {
            throw new IllegalArgumentException("Null value in column '" + columnName + "' at row " + row);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Non-integer value '" + value + "' in column '" + columnName + "' at row " + row);
        }
    }

    public Value<Table> getTable() {
        return getParams().get(tableName);
    }

    public Value<Boolean> getFlag() {
        return getParams().get(flagName);
    }

    public Value<Integer> getNIntervals() {
        return getParams().get(nIntervalsName);
    }
}
