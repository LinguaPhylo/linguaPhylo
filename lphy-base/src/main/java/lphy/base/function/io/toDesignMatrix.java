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
 * <p>Per-deme mode (default): expects columns "deme", "interval", plus predictor columns.
 * Rows sorted by (interval ascending, deme alphabetical).
 *
 * <p>Migration mode (migrationMatrix=true): expects columns "from", "to", "interval",
 * plus predictor columns. Rows sorted by (interval ascending, from-deme alphabetical,
 * to-deme alphabetical), self-pairs skipped.
 */
public class toDesignMatrix extends DeterministicFunction<Double[][]> {

    private static final String tableName = "table";
    private static final String flagName = "migrationMatrix";

    public toDesignMatrix(
            @ParameterInfo(name = tableName, description = "Table read from a CSV file containing predictor columns and index columns (deme/interval or from/to/interval).") Value<Table> table,
            @ParameterInfo(name = flagName, description = "If true, treat as migration (from/to/interval) format; if false (default), treat as per-deme (deme/interval) format.", optional = true) Value<Boolean> flag) {
        if (table == null || table.value().size() == 0)
            throw new IllegalArgumentException("table is null or empty");
        setParam(tableName, table);
        if (flag != null) {
            setParam(flagName, flag);
        }
    }

    @GeneratorInfo(name = "toDesignMatrix", description = "Convert a table from readDelim into a Double[][] design matrix for generalLinearFunction. " +
            "Per-deme mode expects columns 'deme' and 'interval'; migration mode expects 'from', 'to', and 'interval'. " +
            "All remaining columns are treated as predictors. Rows are sorted in canonical order.")
    @Override
    public Value<Double[][]> apply() {
        Table table = getTable().value();
        boolean isMigration = false;
        if (getFlag() != null) {
            isMigration = getFlag().value();
        }

        String[] columnNames = table.getColumnNames();
        if (columnNames == null || columnNames.length < 3)
            throw new IllegalArgumentException("Table must have at least 3 columns (index columns + at least 1 predictor).");

        Double[][] result;
        if (isMigration) {
            result = buildMigrationMatrix(table, columnNames);
        } else {
            result = buildPerDemeMatrix(table, columnNames);
        }

        return new Value<>("", result, this);
    }

    private Double[][] buildPerDemeMatrix(Table table, String[] columnNames) {
        // Find index columns
        int demeCol = findColumn(columnNames, "deme");
        int intervalCol = findColumn(columnNames, "interval");

        // Identify predictor columns (everything except deme and interval)
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

        // Build sortable rows: key = (interval, deme), value = predictor values
        TreeMap<String, double[]> sortedRows = new TreeMap<>();
        for (int r = 0; r < nRows; r++) {
            int interval = parseIntValue(table.getColumn(intervalCol).get(r), "interval", r);
            String deme = String.valueOf(table.getColumn(demeCol).get(r));

            // Compound sort key: interval (zero-padded) + deme
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

        // Convert to Double[][]
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
        // Find index columns
        int fromCol = findColumn(columnNames, "from");
        int toCol = findColumn(columnNames, "to");
        int intervalCol = findColumn(columnNames, "interval");

        // Identify predictor columns (everything except from, to, interval)
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

        // Build sortable rows: key = (interval, from, to), value = predictor values
        TreeMap<String, double[]> sortedRows = new TreeMap<>();
        for (int r = 0; r < nRows; r++) {
            int interval = parseIntValue(table.getColumn(intervalCol).get(r), "interval", r);
            String from = String.valueOf(table.getColumn(fromCol).get(r));
            String to = String.valueOf(table.getColumn(toCol).get(r));

            // Skip self-pairs
            if (from.equals(to)) continue;

            // Compound sort key: interval (zero-padded) + from + to
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

        // Convert to Double[][]
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
}
