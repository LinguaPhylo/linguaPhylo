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

    // translator
    public static String translateRead(int ref, String read) {
        // Reference base lookup (A=0, C=1, G=2, T=3)
        char[] baseSymbols = {'A', 'C', 'G', 'T'};
        if (ref < 0 || ref >= baseSymbols.length) {
            throw new IllegalArgumentException("Invalid ref index: " + ref);
        }
        char refBase = baseSymbols[ref];

        // Initialize counts
        int[] counts = new int[4];

        int i = 0;
        while (i < read.length()) {
            char c = read.charAt(i);

            switch (c) {
                case '.': // match reference (forward)
                case ',': // match reference (reverse)
                    counts[ref]++; // increment reference base count
                    i++;
                    break;

                case '^': // start of read segment (^ + mappingQ)
                    i += 2;
                    break;

                case '$': // end of read segment
                    i++;
                    break;

                case '+': // insertion
                case '-': {
                    i++;
                    int start = i;
                    while (i < read.length() && Character.isDigit(read.charAt(i))) i++;
                    int len = Integer.parseInt(read.substring(start, i));
                    i += len; // skip inserted/deleted sequence
                    break;
                }

                case '<': case '>': case '*': // skip / deletion
                    i++;
                    break;

                default:
                    // A/C/G/T mismatches on forward or reverse strand
                    char base = Character.toUpperCase(c);
                    switch (base) {
                        case 'A': counts[0]++; break;
                        case 'C': counts[1]++; break;
                        case 'G': counts[2]++; break;
                        case 'T': counts[3]++; break;
                        default: break; // ignore N or invalid chars
                    }
                    i++;
                    break;
            }
        }

        // output like "A0:C0:G0:T0"
        return String.format("A%d:C%d:G%d:T%d", counts[0], counts[1], counts[2], counts[3]);
    }

    /*
        translate the mapping quality from ASCII to numeric Phred score
     */
    public int[] translate_mappingQuality(String mappingQuality){
        int[] qualities = new int[mappingQuality.length()];
        for (int i = 0; i < mappingQuality.length(); i++) {
            char q = mappingQuality.charAt(i);
            qualities[i] = (int) q - 33;
        }

        return qualities;
    }
}
