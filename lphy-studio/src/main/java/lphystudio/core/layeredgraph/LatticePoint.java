package lphystudio.core.layeredgraph;

public class LatticePoint {

    public static final String KEY = "latticePoint";

    public int x = 0;
    public int y = 0;

    public LatticePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object object) {
        return object instanceof LatticePoint && ((LatticePoint)object).x == x && ((LatticePoint)object).y == y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
