package lphy.evolution.traits;

public class CharSetBlock {

    private final int from;
    private final int to;
    private final int every;

    public CharSetBlock(int from, int to, int every) {
        this.from = from;
        this.to = to;
        this.every = every;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getEvery() {
        return every;
    }

    @Override
    public String toString() {
        return "CharSet{" +
                "from=" + from +
                ", to=" + to +
                ", every=" + every +
                '}';
    }
}
