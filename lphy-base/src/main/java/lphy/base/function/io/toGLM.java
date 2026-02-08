package lphy.base.function.io;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.Table;

import java.util.*;

public class toGLM extends DeterministicFunction<Double[]> {

    private static final String tableName = "table";
    private static final String flagName = "migrationMatrix";

    public toGLM(
            @ParameterInfo(name = tableName, description = "Table read from a migration matrix CSV: first column = row deme names; remaining columns = numeric rates with column names = deme names.") Value<Table> table,
            @ParameterInfo(name = flagName, description = "The flag showing the table should follow migration matrix rules, default false", optional = true) Value<Boolean> flag) {
        if (table == null || table.value().size() == 0) throw new IllegalArgumentException("table is null or empty");
        if (table.value().getColumnNames().length < 2) throw new IllegalArgumentException("table has less than 2 columns");
        setParam(tableName, table);
        if (flag != null) {
            setParam(flagName, flag);
        }
    }

    @GeneratorInfo(name = "toGLM", description = "convert the table generated from readDelim function to a flattered double array for using in StructuredCoalescentRateShifts distribution.")
    @Override
    public Value<Double[]> apply() {
        Table table = getTable().value();
        boolean flag = false;
        if (getFlag() != null) {
            flag = getFlag().value();
        }

        // get row names as demes (first column)
        List demes = table.getColumn(0);
        String[] columnNames = table.getColumnNames();
        if (columnNames == null || columnNames.length < 2) throw new IllegalArgumentException("table doesn't have column names");

        // rearrange the table, make it divide by intervals
        // check no columns missing for all intervals
        if (flag) {
            int nCols = columnNames.length - 1;
            int nDemes = demes.size();
            if (nCols % nDemes != 0) {
                throw new IllegalArgumentException("Invalid table layout: expected (columns-1) to be divisible by number of demes.\n" +
                        "columns-1 = " + nCols + ", demes = " + nDemes + ".\n" + "Check your input file or deme definition.");
            }
        }

        // get the row names as string
        // make sure the names are String and store the original order
        // will do index map afterwards
        List<String> originalRowDemes = new ArrayList<>(demes.size());
        for (Object o : demes) originalRowDemes.add(String.valueOf(o));

        Map<String, Integer> rowIndexByDeme = new HashMap<>();
        for (int i = 0; i < originalRowDemes.size(); i++) {
            String d = originalRowDemes.get(i);
            if (rowIndexByDeme.put(d, i) != null) {
                throw new IllegalArgumentException("Duplicate row deme name found: " + d);
            }
        }

        // Sorted row demes
        List<String> sortedRowDemes = new ArrayList<>(rowIndexByDeme.keySet());
        Collections.sort(sortedRowDemes);

        if (flag) {
            List<Double> result = getMigrationMatrix(columnNames, table, sortedRowDemes, rowIndexByDeme);

            Double[] m_data = result.toArray(new Double[result.size()]);
//            for (int i = 0; i<m_data.length; i++) {
//                System.out.println(m_data[i]);
//            }
            return new Value<>("", m_data, this);

        } else {
            // Parse interval columns into TreeMap so intervals are sorted ascending
            List<Double> result = getFlatArray(columnNames, table, originalRowDemes, sortedRowDemes, rowIndexByDeme);

            Double[] array = result.toArray(new Double[result.size()]);
//            for (int i = 0; i< array.length; i++) {
//                System.out.println(array[i]);
//            }
            return new Value<>("", array, this);
        }
    }

    private static List<Double> getMigrationMatrix(String[] columnNames, Table table, List<String> sortedRowDemes, Map<String, Integer> rowIndexByDeme) {
        // use tree map to order the intervals ascending (recent -> ancient)
        Map<Integer, Map<String, List<?>>> columnsByInterval = new TreeMap<>();
        for (int i = 1; i < columnNames.length; i++) { // ignore the first cell for row names

            // get the column name and extract interval
            String name = columnNames[i];
            if (name == null) {
                throw new IllegalArgumentException("Null column name at index " + i);
            }

            String[] parts = name.split(":", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid column name '" + name + "'. Expect format 'deme:interval'."
                );
            }

            String deme = parts[0].trim();
            String intervalStr = parts[1].trim();

            if (deme.isEmpty()) {
                throw new IllegalArgumentException("Invalid column name '" + name + "': deme part is empty.");
            }

            int interval;
            try {
                interval = Integer.parseInt(intervalStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid column name '" + name + "': interval '" + intervalStr + "' is not an integer."
                );
            }

            // if everything parsed correctly, extract that column, and put it into tree map
            List<?> column = table.getColumn(name);
            if (column == null) {
                throw new IllegalStateException("Internal error: cannot retrieve column '" + name + "'.");
            }

            columnsByInterval.computeIfAbsent(interval, k -> new TreeMap<>()).put(deme, column);
        }

        if (columnsByInterval.isEmpty()) {
            throw new IllegalArgumentException("No data columns found (after the first deme column).");
        }

        // map the flattened array
        List<Double> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, List<?>>> entry : columnsByInterval.entrySet()) {
            int interval = entry.getKey();
            Map<String, List<?>> columns = entry.getValue();

            for (String fromDeme : sortedRowDemes) {
                int oldRowIndex = rowIndexByDeme.get(fromDeme);

                for (String toDeme : sortedRowDemes) {
                    if (toDeme.equals(fromDeme)) continue; // skip A to A

                    List<?> column = columns.get(toDeme);
                    if (column == null) {
                        throw new IllegalStateException("Internal error: cannot retrieve column '" + toDeme + "at interval" + interval + "'.");
                    }

                    Object rate = column.get(oldRowIndex);
                    if (rate == null) {
                        throw new IllegalArgumentException("Null migration rate for " + fromDeme + "→" + toDeme + " at interval " + interval);
                    }

                    double value;
                    if (rate instanceof Number) {
                        value = ((Number) rate).doubleValue();
                    } else {
                        try {
                            value = Double.parseDouble(rate.toString());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Non-numeric migration rate for " + fromDeme + "<UNK>" + toDeme + " at interval " + interval);
                        }
                    }
                    result.add(value);
                }
            }
        }
        return result;
    }

    private static List<Double> getFlatArray(String[] columnNames, Table table, List<String> originalRowDemes, List<String> sortedRowDemes, Map<String, Integer> rowIndexByDeme) {
        Map<Integer, List<?>> columnByInterval = new TreeMap<>();
        for (int i = 1; i < columnNames.length; i++) {
            String name = columnNames[i];
            if (name == null) {
                throw new IllegalArgumentException("Null column name at index " + i);
            }

            int interval;
            try {
                interval = Integer.parseInt(name.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid column name '" + name + "'. When migrationMatrix=false, expect integer interval column names only."
                );
            }

            List<?> col = table.getColumn(name);
            if (col == null) {
                throw new IllegalStateException("Internal error: cannot retrieve column '" + name + "'.");
            }
            if (col.size() != originalRowDemes.size()) {
                throw new IllegalArgumentException(
                        "Interval column '" + name + "' length mismatch: expected " + originalRowDemes.size() +
                                " rows (demes) but got " + col.size()
                );
            }
            columnByInterval.put(interval, col);
        }

        if (columnByInterval.isEmpty()) {
            throw new IllegalArgumentException("No interval columns found (after the first deme column).");
        }

        // Flatten: for each interval (ascending), for each deme (ascending), take value at that deme's original row index
        List<Double> result = new ArrayList<>();
        for (Map.Entry<Integer, List<?>> e : columnByInterval.entrySet()) {
            int interval = e.getKey();
            List<?> col = e.getValue();

            for (String deme : sortedRowDemes) {
                Integer oldRowIndex = rowIndexByDeme.get(deme);
                if (oldRowIndex == null) {
                    throw new IllegalStateException("Internal error: cannot find row index for deme '" + deme + "'.");
                }

                Object rate = col.get(oldRowIndex);
                if (rate == null) {
                    throw new IllegalArgumentException("Null value for deme " + deme + " at interval " + interval);
                }

                double value;
                if (rate instanceof Number) {
                    value = ((Number) rate).doubleValue();
                } else {
                    try {
                        value = Double.parseDouble(rate.toString());
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(
                                "Non-numeric value for deme " + deme + " at interval " + interval + ": '" + rate + "'"
                        );
                    }
                }
                result.add(value);
            }
        }
        return result;
    }

    public Value<Table> getTable() {
        return getParams().get(tableName);
    }

    public Value<Boolean> getFlag() {
        return getParams().get(flagName);
    }
}
