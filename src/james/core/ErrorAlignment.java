package james.core;

import james.app.AlignmentComponent;
import james.app.HasComponentView;
import james.graphicalModel.Value;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class ErrorAlignment extends Alignment {

    Alignment parent;

    public ErrorAlignment(int taxa, int length, Map<String, Integer> idMap, Alignment parent) {
        super(taxa, length, idMap, parent.numStates);

        this.parent = parent;
    }

    public boolean isError(int i, int j) {
        return alignment[i][j] != parent.alignment[i][j];
    }
}
