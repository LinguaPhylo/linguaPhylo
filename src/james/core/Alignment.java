package james.core;

import james.graphicalModel.Value;
import james.app.AlignmentComponent;
import james.app.HasComponentView;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class Alignment implements HasComponentView<Alignment> {

    Integer[][] alignment;
    Map<String, Integer> idMap;
    Map<Integer, String> reverseMap;
    int numStates;

    public Alignment(int taxa, int length, Map<String, Integer> idMap, int numStates) {
        alignment = new Integer[taxa][length];
        this.idMap = idMap;

        reverseMap = new TreeMap<>();
        for (String key : idMap.keySet()) {
            reverseMap.put(idMap.get(key), key);
        }

        this.numStates = numStates;
    }

    public void setState(int taxon, int position, int state) {

        if (state < 0 || state > numStates-1) throw new IllegalArgumentException("Tried to set a state outside of the range!");
        alignment[taxon][position] = state;
    }

    public void setState(String taxon, int position, int state) {
        alignment[idMap.get(taxon)][position] = state;
    }

    public Integer getState(int taxon, int position) {
        return alignment[taxon][position];
    }

    @Override
    public JComponent getComponent(Value<Alignment> value) {

        if (numStates == 2)
            return new AlignmentComponent(value, AlignmentComponent.BINARY_COLORS);
        else return new AlignmentComponent(value, AlignmentComponent.DNA_COLORS);
    }

    public int n() {
        return alignment.length;
    }

    public int L() {
        return alignment[0].length;
    }

    public String getId(int taxonIndex) {
        return reverseMap.get(taxonIndex);
    }

    public String toJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n(); i++) {
            builder.append("  ");
            builder.append(reverseMap.get(i));
            builder.append(" = ");
            builder.append(Arrays.toString(alignment[i]));
            if (i < n()-1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("}");
        return builder.toString();
    }
}
