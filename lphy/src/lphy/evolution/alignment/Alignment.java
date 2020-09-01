package lphy.evolution.alignment;

import lphy.app.AlignmentComponent;
import lphy.app.HasComponentView;
import lphy.evolution.DataFrame;
import lphy.evolution.Taxa;
import lphy.evolution.alignment.datatype.DataType;
import lphy.evolution.alignment.datatype.TwoStates;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Alignment extends DataFrame implements Taxa, HasComponentView<Alignment> {

    int[][] alignment;
    Map<String, Integer> idMap;
    Map<Integer, String> reverseMap;

    DataType dataType; // encapsulate stateCount, ambiguousState, and getChar() ...

    @Deprecated
    // for simulators
    public Alignment(int ntaxa, int nchar, Map<String, Integer> idMap, int numStates) {
        super(ntaxa, nchar);
        this.idMap = idMap;
        fillRevMap();

        alignment = new int[ntaxa][nchar];
        dataType = DataType.guessDataType(numStates);
    }

    // for inheritance
    public Alignment() {  }

    public Alignment(int ntaxa, int nchar, Map<String, Integer> idMap, DataType dataType) {
        super(ntaxa, nchar);
        this.idMap = idMap;
        fillRevMap();

        alignment = new int[ntaxa][nchar];
        this.dataType = dataType;
    }

    // for inheritance
    protected void fillRevMap() {
        reverseMap = new TreeMap<>();
        for (String key : idMap.keySet()) {
            reverseMap.put(idMap.get(key), key);
        }
    }

    public String[] getTaxa() {
        String[] taxa = new String[ntaxa()];
        for (int i = 0; i < ntaxa(); i++) {
            taxa[i] = reverseMap.get(i);
        }
        return taxa;
    }

    /**
     * Set states to {@link #alignment}.
     * @param taxon      the index of taxon in the 1st dimension of {@link #alignment}.
     * @param position   the site position in the 2nd dimension of {@link #alignment}.
     * @param state      the state in integer
     * @param ambiguous  if false, then the state is restricted to the integers between 0 and stateCount,
     *                   normally used in simulation.
     *                   if true, then the ambiguous states are allowed, normally used by imported data.
     */
    public void setState(int taxon, int position, int state, boolean ambiguous) {

        if (state < 0 || state > dataType.getStateCount()-1) {
            if (ambiguous && state < dataType.getAmbiguousStateCount())
                System.err.println("There is ambiguous state " + state + " = " + dataType.getChar(state));
            else
                throw new IllegalArgumentException("Tried to set a state outside of the range! state = " + state);
        }
        alignment[taxon][position] = state;
    }

    public void setState(String taxon, int position, int state, boolean ambiguous) {
        setState(idMap.get(taxon), position, state, ambiguous);
    }

    public int getState(int taxon, int position) {
        return alignment[taxon][position];
    }

    @Override
    public JComponent getComponent(Value<Alignment> value) {

        if (dataType instanceof TwoStates)
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

    public int getSiteCount() {
        return L();
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getDataTypeDescription() {
        return dataType.getDescription();
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
            builder.append(dataType.getChar(alignment[taxonIndex][j]));
        }
        return builder.toString();
    }

}
