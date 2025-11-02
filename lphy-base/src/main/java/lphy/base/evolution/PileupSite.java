package lphy.base.evolution;

/*
    Representing the pipleup site for one cell
 */
public class PileupSite {
    private CellPosition cellPosition;
    private int ref;
    private CellPileupData cellPileupData;

    public record CellPileupData(int readCount, String reads, String mappingQuality) {
        @Override
            public String toString() {
                return String.format("(reads=%s, count=%d, mapQ=%s)",
                        reads, readCount, mappingQuality);
            }

    }

    public PileupSite(CellPosition cellPosition, int ref, CellPileupData cellPileupData) {
        this.cellPosition = cellPosition;
        this.ref = ref;
        this.cellPileupData = cellPileupData;
    }

    // getters and setters
    public CellPosition getCellPosition() {
        return cellPosition;
    }
    public void setCellPosition(CellPosition cellPosition) {
        this.cellPosition = cellPosition;
    }

    public int getRef() {
        return ref;
    }
    public void setRef(int ref) {
        this.ref = ref;
    }

    public CellPileupData getCellPileupData() {
        return cellPileupData;
    }
    public void setCellPileupData(CellPileupData cellPileupData) {
        this.cellPileupData = cellPileupData;
    }
}
