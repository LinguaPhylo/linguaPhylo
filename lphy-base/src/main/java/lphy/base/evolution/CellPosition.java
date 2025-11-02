package lphy.base.evolution;

import java.util.Objects;

public class CellPosition {
    private String chromName;
    private String cellName;
    private int position;

    public CellPosition(String cellName, int position) {
        this.chromName = cellName;
        this.cellName = cellName;
        this.position = position;
    }

    public CellPosition(String chromName, String cellName, int position) {
        this.chromName = null;
        this.cellName = cellName;
        this.position = position;
    }

    public String getChromName() {
        return chromName;
    }
    public void setChromName(String chromName) {
        this.chromName = chromName;
    }

    public String getCellName() {
        return cellName;
    }
    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
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
        return Objects.hash(chromName, cellName, position);
    }

    @Override
    public String toString() {
        if (chromName != null) {
            return "(" + chromName + "_" + cellName + ", " + position + ")";
        } else {
            return "(" + cellName + ", " + position + ")";
        }
    }
}
