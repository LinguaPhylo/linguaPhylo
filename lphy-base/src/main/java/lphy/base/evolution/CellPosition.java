package lphy.base.evolution;

import java.util.Objects;

public class CellPosition {
    private String cellName;
    private int position;

    public CellPosition(String cellName, int position) {
        this.cellName = cellName;
        this.position = position;
    }

    public String getCellName() {
        return cellName;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CellPosition that = (CellPosition) obj;
        return position == that.position && Objects.equals(cellName, that.cellName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cellName, position);
    }

    @Override
    public String toString() {
        return "(" + cellName + ", " + position + ")";
    }
}
