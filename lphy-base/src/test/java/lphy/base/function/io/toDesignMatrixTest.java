package lphy.base.function.io;

import lphy.core.model.Value;
import lphy.core.model.datatype.Table;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class toDesignMatrixTest {

    /**
     * Build a Table from column names and row data.
     * Each column is a List of values parsed via Table.getValueGuessType.
     */
    private Table buildTable(String[] columnNames, String[][] rows) {
        Table table = new Table();
        for (int c = 0; c < columnNames.length; c++) {
            List<Object> col = new ArrayList<>();
            for (String[] row : rows) {
                col.add(Table.getValueGuessType(row[c]));
            }
            table.put(columnNames[c], col);
        }
        return table;
    }

    @Test
    void testPerDemeMode() {
        // 3 demes, 2 intervals, 2 predictors
        // Rows deliberately NOT in canonical order to test sorting
        String[] colNames = {"deme", "interval", "log_area", "urbanization"};
        String[][] rows = {
                {"C", "0", "1.8", "0.6"},
                {"A", "0", "2.0", "0.8"},
                {"B", "0", "1.5", "0.5"},
                {"B", "1", "1.5", "0.1"},
                {"C", "1", "1.8", "0.15"},
                {"A", "1", "2.0", "0.2"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("ne_table", table);

        toDesignMatrix func = new toDesignMatrix(tableValue, null);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // Expected: 6 rows (3 demes x 2 intervals), 2 predictor columns
        assertEquals(6, matrix.length, "Should have 6 rows");
        assertEquals(2, matrix[0].length, "Should have 2 predictor columns");

        // Expected canonical order: interval 0 (A, B, C), interval 1 (A, B, C)
        // interval 0, deme A: [2.0, 0.8]
        assertArrayEquals(new Double[]{2.0, 0.8}, matrix[0]);
        // interval 0, deme B: [1.5, 0.5]
        assertArrayEquals(new Double[]{1.5, 0.5}, matrix[1]);
        // interval 0, deme C: [1.8, 0.6]
        assertArrayEquals(new Double[]{1.8, 0.6}, matrix[2]);
        // interval 1, deme A: [2.0, 0.2]
        assertArrayEquals(new Double[]{2.0, 0.2}, matrix[3]);
        // interval 1, deme B: [1.5, 0.1]
        assertArrayEquals(new Double[]{1.5, 0.1}, matrix[4]);
        // interval 1, deme C: [1.8, 0.15]
        assertArrayEquals(new Double[]{1.8, 0.15}, matrix[5]);
    }

    @Test
    void testMigrationMode() {
        // 3 demes (6 directed pairs), 2 intervals, 2 predictors
        // Rows deliberately shuffled to test sorting
        String[] colNames = {"from", "to", "interval", "inverse_distance", "connectivity"};
        String[][] rows = {
                {"B", "C", "0", "1.5", "0.6"},
                {"A", "B", "0", "0.5", "0.9"},
                {"C", "A", "0", "2.0", "0.5"},
                {"A", "C", "0", "2.0", "0.5"},
                {"C", "B", "0", "1.5", "0.6"},
                {"B", "A", "0", "0.5", "0.9"},
                {"C", "B", "1", "1.5", "0.2"},
                {"A", "B", "1", "0.5", "0.3"},
                {"B", "A", "1", "0.5", "0.3"},
                {"A", "C", "1", "2.0", "0.1"},
                {"B", "C", "1", "1.5", "0.2"},
                {"C", "A", "1", "2.0", "0.1"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("m_table", table);
        Value<Boolean> flag = new Value<>("migrationMatrix", true);

        toDesignMatrix func = new toDesignMatrix(tableValue, flag);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // Expected: 12 rows (6 pairs x 2 intervals), 2 predictor columns
        assertEquals(12, matrix.length, "Should have 12 rows");
        assertEquals(2, matrix[0].length, "Should have 2 predictor columns");

        // Expected canonical order: interval 0 then interval 1,
        // within each interval: A->B, A->C, B->A, B->C, C->A, C->B

        // Interval 0
        assertArrayEquals(new Double[]{0.5, 0.9}, matrix[0], "A->B int0");
        assertArrayEquals(new Double[]{2.0, 0.5}, matrix[1], "A->C int0");
        assertArrayEquals(new Double[]{0.5, 0.9}, matrix[2], "B->A int0");
        assertArrayEquals(new Double[]{1.5, 0.6}, matrix[3], "B->C int0");
        assertArrayEquals(new Double[]{2.0, 0.5}, matrix[4], "C->A int0");
        assertArrayEquals(new Double[]{1.5, 0.6}, matrix[5], "C->B int0");

        // Interval 1
        assertArrayEquals(new Double[]{0.5, 0.3}, matrix[6], "A->B int1");
        assertArrayEquals(new Double[]{2.0, 0.1}, matrix[7], "A->C int1");
        assertArrayEquals(new Double[]{0.5, 0.3}, matrix[8], "B->A int1");
        assertArrayEquals(new Double[]{1.5, 0.2}, matrix[9], "B->C int1");
        assertArrayEquals(new Double[]{2.0, 0.1}, matrix[10], "C->A int1");
        assertArrayEquals(new Double[]{1.5, 0.2}, matrix[11], "C->B int1");
    }

    @Test
    void testSelfPairsSkipped() {
        // Include self-pairs, verify they are filtered out
        String[] colNames = {"from", "to", "interval", "predictor"};
        String[][] rows = {
                {"A", "A", "0", "1.0"},
                {"A", "B", "0", "2.0"},
                {"B", "A", "0", "3.0"},
                {"B", "B", "0", "4.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("table", table);
        Value<Boolean> flag = new Value<>("migrationMatrix", true);

        toDesignMatrix func = new toDesignMatrix(tableValue, flag);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // Self-pairs (A->A, B->B) should be skipped
        assertEquals(2, matrix.length, "Should have 2 rows (self-pairs removed)");
        assertArrayEquals(new Double[]{2.0}, matrix[0], "A->B");
        assertArrayEquals(new Double[]{3.0}, matrix[1], "B->A");
    }

    @Test
    void testMissingColumnThrows() {
        // Per-deme mode without 'interval' column
        String[] colNames = {"deme", "predictor1"};
        String[][] rows = {
                {"A", "1.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("table", table);

        assertThrows(IllegalArgumentException.class, () -> {
            toDesignMatrix func = new toDesignMatrix(tableValue, null);
            func.apply();
        });
    }

    @Test
    void testDuplicateRowThrows() {
        String[] colNames = {"deme", "interval", "predictor"};
        String[][] rows = {
                {"A", "0", "1.0"},
                {"A", "0", "2.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("table", table);

        toDesignMatrix func = new toDesignMatrix(tableValue, null);
        assertThrows(IllegalArgumentException.class, func::apply);
    }
}
