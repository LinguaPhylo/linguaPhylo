package james.core;

import james.swing.AlignmentComponent;
import james.swing.HasComponentView;

import javax.swing.*;
import java.util.Map;

/**
 * Created by adru001 on 2/02/20.
 */
public class Alignment implements HasComponentView {

    Integer[][] alignment;
    Map<String, Integer> idMap;

    public Alignment(int taxa, int length, Map<String, Integer> idMap) {
        alignment = new Integer[taxa][length];
        this.idMap = idMap;
    }

    public void setState(int taxon, int position, int state) {
        alignment[taxon][position] = state;
    }

    public void setState(String taxon, int position, int state) {
        alignment[idMap.get(taxon)][position] = state;
    }

    public Integer getState(int taxon, int position) {
        return alignment[taxon][position];
    }

    @Override
    public JComponent getComponent() {
        return new AlignmentComponent(this, AlignmentComponent.DNA_COLORS);
    }

    public int n() {
        return alignment.length;
    }

    public int L() {
        return alignment[0].length;
    }
}
