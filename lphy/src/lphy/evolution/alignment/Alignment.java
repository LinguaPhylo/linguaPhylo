package lphy.evolution.alignment;

import lphy.evolution.DataFrame;
import lphy.evolution.NTaxa;
import lphy.graphicalModel.Value;
import lphy.app.AlignmentComponent;
import lphy.app.HasComponentView;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class Alignment extends DataFrame implements NTaxa, HasComponentView<Alignment> {

    int[][] alignment;
    Map<String, Integer> idMap;
    Map<Integer, String> reverseMap;
    int numStates;

    // for nex
    public Alignment(int ntaxa, int nchar, Map<String, Integer> idMap) {
        super(ntaxa, nchar);

        alignment = new int[ntaxa][nchar];
        this.idMap = idMap;

        fillRevMap(idMap);
    }

    // for inheritance
    protected void fillRevMap(Map<String, Integer> idMap) {
        reverseMap = new TreeMap<>();
        for (String key : idMap.keySet()) {
            reverseMap.put(idMap.get(key), key);
        }
    }

    // for simulators
    public Alignment(int ntaxa, int nchar, Map<String, Integer> idMap, int numStates) {
        this(ntaxa, nchar, idMap);
        this.numStates = numStates;
    }

    public void setState(int taxon, int position, int state) {

        if (state < 0 || state > numStates-1) throw new IllegalArgumentException("Tried to set a state outside of the range! ");
        alignment[taxon][position] = state;
    }

    public void setState(String taxon, int position, int state) {

        alignment[idMap.get(taxon)][position] = state;
    }

    public int getState(int taxon, int position) {
        return alignment[taxon][position];
    }

    @Override
    public JComponent getComponent(Value<Alignment> value) {

        if (numStates == 2)
            return new AlignmentComponent(value, AlignmentComponent.BINARY_COLORS);
        else return new AlignmentComponent(value, AlignmentComponent.DNA_COLORS);
    }

    public final int n() {
        return alignment.length;
    }

    public final int L() {
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

    public int getSiteCount() {
        return L();
    }

    public String getDataTypeDescription() {
        switch (numStates) {
            case 2: return "binary";
            case 4: return "nucleotide";
            default: return "standard";
        }
    }

    public String[] getTaxaNames() {
        String[] taxaNames = new String[n()];
        for (int i = 0; i < taxaNames.length; i++) {
            taxaNames[i] = reverseMap.get(i);
        }
        return taxaNames;
    }

    public String getSequence(int taxonIndex) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < alignment[taxonIndex].length; j++) {
            builder.append(getChar(alignment[taxonIndex][j]));
        }
        return builder.toString();
    }

    private char getChar(int state) {
        if (numStates == 4) {
            return DNA[state];
        }
        return (char)('0' + state);
    }

    char[] DNA = {'A', 'C', 'G', 'T'};

    public void setNumStates(int numStates) {
        this.numStates = numStates;
    }
}
