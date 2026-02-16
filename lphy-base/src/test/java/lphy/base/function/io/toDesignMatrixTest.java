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

        toDesignMatrix func = new toDesignMatrix(tableValue, null, null);
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

        toDesignMatrix func = new toDesignMatrix(tableValue, flag, null);
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

        toDesignMatrix func = new toDesignMatrix(tableValue, flag, null);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // Self-pairs (A->A, B->B) should be skipped
        assertEquals(2, matrix.length, "Should have 2 rows (self-pairs removed)");
        assertArrayEquals(new Double[]{2.0}, matrix[0], "A->B");
        assertArrayEquals(new Double[]{3.0}, matrix[1], "B->A");
    }

    @Test
    void testUnrecognizedFormatThrows() {
        // 3 columns with no recognized format pattern
        String[] colNames = {"x", "y", "z"};
        String[][] rows = {
                {"1.0", "2.0", "3.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("table", table);

        assertThrows(IllegalArgumentException.class, () -> {
            toDesignMatrix func = new toDesignMatrix(tableValue, null, null);
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

        toDesignMatrix func = new toDesignMatrix(tableValue, null, null);
        assertThrows(IllegalArgumentException.class, func::apply);
    }

    // ---- MASCOT format tests ----

    @Test
    void testMascotNeVector() {
        // Ne_Pdens-style: 2 columns (read with header=false), deme names + values
        // Deliberately unsorted to test alphabetical sorting
        String[] colNames = {"Column1", "Column2"};
        String[][] rows = {
                {"Kenema", "-0.700119"},
                {"Bombali", "-0.438346"},
                {"WesternUrban", "1.775313"},
                {"Kailahun", "-0.484102"},
                {"WesternRural", "0.601835"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("ne_table", table);

        toDesignMatrix func = new toDesignMatrix(tableValue, null, null);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // 5 demes, 1 predictor => 5 rows, 1 column
        assertEquals(5, matrix.length);
        assertEquals(1, matrix[0].length);

        // Alphabetical order: Bombali, Kailahun, Kenema, WesternRural, WesternUrban
        assertEquals(-0.438346, matrix[0][0], 1e-9, "Bombali");
        assertEquals(-0.484102, matrix[1][0], 1e-9, "Kailahun");
        assertEquals(-0.700119, matrix[2][0], 1e-9, "Kenema");
        assertEquals(0.601835, matrix[3][0], 1e-9, "WesternRural");
        assertEquals(1.775313, matrix[4][0], 1e-9, "WesternUrban");
    }

    @Test
    void testMascotNeMatrix() {
        // Ne_cases-style: header starts with blank, remaining columns are interval indices
        // 3 demes, 2 intervals
        String[] colNames = {"", "0", "1"};
        String[][] rows = {
                {"C", "3.0", "6.0"},
                {"A", "1.0", "4.0"},
                {"B", "2.0", "5.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("ne_table", table);

        toDesignMatrix func = new toDesignMatrix(tableValue, null, null);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // 3 demes x 2 intervals = 6 rows, 1 predictor column
        assertEquals(6, matrix.length);
        assertEquals(1, matrix[0].length);

        // Sorted by (interval ascending, deme alphabetical):
        // int 0: A=1.0, B=2.0, C=3.0; int 1: A=4.0, B=5.0, C=6.0
        assertEquals(1.0, matrix[0][0], 1e-9, "int0 A");
        assertEquals(2.0, matrix[1][0], 1e-9, "int0 B");
        assertEquals(3.0, matrix[2][0], 1e-9, "int0 C");
        assertEquals(4.0, matrix[3][0], 1e-9, "int1 A");
        assertEquals(5.0, matrix[4][0], 1e-9, "int1 B");
        assertEquals(6.0, matrix[5][0], 1e-9, "int1 C");
    }

    @Test
    void testMascotSquareMigrationMatrix() {
        // Square migration matrix: header starts with blank, remaining columns are to-deme names
        // 3 demes, self-pairs (diagonal) should be skipped
        String[] colNames = {"", "A", "B", "C"};
        String[][] rows = {
                {"A", "0.0", "1.5", "2.5"},
                {"B", "3.5", "0.0", "4.5"},
                {"C", "5.5", "6.5", "0.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("table", table);
        Value<Boolean> flag = new Value<>("migrationMatrix", true);

        toDesignMatrix func = new toDesignMatrix(tableValue, flag, null);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // 3*(3-1) = 6 rows (self-pairs skipped), 1 predictor column
        assertEquals(6, matrix.length);
        assertEquals(1, matrix[0].length);

        // Sorted by (from alphabetical, to alphabetical):
        // A->B=1.5, A->C=2.5, B->A=3.5, B->C=4.5, C->A=5.5, C->B=6.5
        assertEquals(1.5, matrix[0][0], 1e-9, "A->B");
        assertEquals(2.5, matrix[1][0], 1e-9, "A->C");
        assertEquals(3.5, matrix[2][0], 1e-9, "B->A");
        assertEquals(4.5, matrix[3][0], 1e-9, "B->C");
        assertEquals(5.5, matrix[4][0], 1e-9, "C->A");
        assertEquals(6.5, matrix[5][0], 1e-9, "C->B");
    }

    @Test
    void testMascotTimeVariantMigrationMatrix() {
        // Time-variant migration matrix: column names encode "to_deme:interval"
        // 3 demes, 2 intervals
        String[] colNames = {"", "A:0", "B:0", "C:0", "A:1", "B:1", "C:1"};
        String[][] rows = {
                {"A", "0.0", "1.0", "2.0", "0.0", "7.0", "8.0"},
                {"B", "3.0", "0.0", "4.0", "9.0", "0.0", "10.0"},
                {"C", "5.0", "6.0", "0.0", "11.0", "12.0", "0.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("table", table);
        Value<Boolean> flag = new Value<>("migrationMatrix", true);

        toDesignMatrix func = new toDesignMatrix(tableValue, flag, null);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // 3*(3-1)*2 = 12 rows, 1 predictor column
        assertEquals(12, matrix.length);
        assertEquals(1, matrix[0].length);

        // Sorted by (interval ascending, from alphabetical, to alphabetical):
        // int 0: A->B=1.0, A->C=2.0, B->A=3.0, B->C=4.0, C->A=5.0, C->B=6.0
        assertEquals(1.0, matrix[0][0], 1e-9, "int0 A->B");
        assertEquals(2.0, matrix[1][0], 1e-9, "int0 A->C");
        assertEquals(3.0, matrix[2][0], 1e-9, "int0 B->A");
        assertEquals(4.0, matrix[3][0], 1e-9, "int0 B->C");
        assertEquals(5.0, matrix[4][0], 1e-9, "int0 C->A");
        assertEquals(6.0, matrix[5][0], 1e-9, "int0 C->B");

        // int 1: A->B=7.0, A->C=8.0, B->A=9.0, B->C=10.0, C->A=11.0, C->B=12.0
        assertEquals(7.0, matrix[6][0], 1e-9, "int1 A->B");
        assertEquals(8.0, matrix[7][0], 1e-9, "int1 A->C");
        assertEquals(9.0, matrix[8][0], 1e-9, "int1 B->A");
        assertEquals(10.0, matrix[9][0], 1e-9, "int1 B->C");
        assertEquals(11.0, matrix[10][0], 1e-9, "int1 C->A");
        assertEquals(12.0, matrix[11][0], 1e-9, "int1 C->B");
    }

    // ---- nIntervals tiling tests ----

    @Test
    void testMascotNeVectorWithNIntervals() {
        // 3 demes tiled across 3 intervals => 9 rows
        String[] colNames = {"Column1", "Column2"};
        String[][] rows = {
                {"C", "3.0"},
                {"A", "1.0"},
                {"B", "2.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("ne_table", table);
        Value<Integer> nIntervals = new Value<>("nIntervals", 3);

        toDesignMatrix func = new toDesignMatrix(tableValue, null, nIntervals);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // 3 demes x 3 intervals = 9 rows, 1 column
        assertEquals(9, matrix.length, "Should have 9 rows");
        assertEquals(1, matrix[0].length, "Should have 1 column");

        // Each interval repeats the same block (A=1.0, B=2.0, C=3.0)
        for (int interval = 0; interval < 3; interval++) {
            int base = interval * 3;
            assertEquals(1.0, matrix[base][0], 1e-9, "int" + interval + " A");
            assertEquals(2.0, matrix[base + 1][0], 1e-9, "int" + interval + " B");
            assertEquals(3.0, matrix[base + 2][0], 1e-9, "int" + interval + " C");
        }
    }

    @Test
    void testMascotSquareMigrationWithNIntervals() {
        // 2 demes tiled across 2 intervals => 2*(2-1)*2 = 4 rows
        String[] colNames = {"", "A", "B"};
        String[][] rows = {
                {"A", "0.0", "1.5"},
                {"B", "3.5", "0.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("table", table);
        Value<Boolean> flag = new Value<>("migrationMatrix", true);
        Value<Integer> nIntervals = new Value<>("nIntervals", 2);

        toDesignMatrix func = new toDesignMatrix(tableValue, flag, nIntervals);
        Value<Double[][]> result = func.apply();
        Double[][] matrix = result.value();

        // 2 pairs (A->B, B->A) x 2 intervals = 4 rows
        assertEquals(4, matrix.length, "Should have 4 rows");
        assertEquals(1, matrix[0].length, "Should have 1 column");

        // interval 0: A->B=1.5, B->A=3.5
        assertEquals(1.5, matrix[0][0], 1e-9, "int0 A->B");
        assertEquals(3.5, matrix[1][0], 1e-9, "int0 B->A");
        // interval 1: same block repeated
        assertEquals(1.5, matrix[2][0], 1e-9, "int1 A->B");
        assertEquals(3.5, matrix[3][0], 1e-9, "int1 B->A");
    }

    @Test
    void testNIntervalsWithTimeVariantThrows() {
        // Ne matrix (already time-variant) + nIntervals => should throw
        String[] colNames = {"", "0", "1"};
        String[][] rows = {
                {"A", "1.0", "2.0"},
                {"B", "3.0", "4.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("ne_table", table);
        Value<Integer> nIntervals = new Value<>("nIntervals", 3);

        toDesignMatrix func = new toDesignMatrix(tableValue, null, nIntervals);
        assertThrows(IllegalArgumentException.class, func::apply,
                "Should throw when nIntervals used with time-variant Ne matrix");
    }

    @Test
    void testNIntervalsWithLongFormatThrows() {
        // Long per-deme format + nIntervals => should throw
        String[] colNames = {"deme", "interval", "predictor"};
        String[][] rows = {
                {"A", "0", "1.0"},
                {"B", "0", "2.0"},
        };

        Table table = buildTable(colNames, rows);
        Value<Table> tableValue = new Value<>("table", table);
        Value<Integer> nIntervals = new Value<>("nIntervals", 3);

        toDesignMatrix func = new toDesignMatrix(tableValue, null, nIntervals);
        assertThrows(IllegalArgumentException.class, func::apply,
                "Should throw when nIntervals used with long format");
    }
}
