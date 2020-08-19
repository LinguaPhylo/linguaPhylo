package lphy.evolution;

public class DataFrame implements NTaxa, NChar {

    int ntaxa;
    int nchar;

    public DataFrame(int ntaxa, int nchar) {
        this.ntaxa = ntaxa;
        this.nchar = nchar;
    }

    @Override
    public int ntaxa() {
        return ntaxa;
    }

    @Override
    public int nchar() {
        return nchar;
    }

    public String toString() {
        return ntaxa + " by " + nchar;
    }
}
