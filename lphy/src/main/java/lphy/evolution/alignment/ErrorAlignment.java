package lphy.evolution.alignment;

import java.util.Map;

/**
 * Created by adru001 on 2/02/20.
 */
public class ErrorAlignment extends SimpleAlignment {

    Alignment parent;

    public ErrorAlignment(int length, Alignment parent) {
        super(length, parent); // copy source

        this.parent = parent;
    }

    public boolean isError(int i, int j) {
        return alignment[i][j] != parent.getState(i,j);
    }
}
