package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Alignment extends AbstractAlignment {

    int[][] alignment;

    // for simulators, and datatype not available
    @Deprecated
    public Alignment(Map<String, Integer> idMap, int nchar, int numStates) {
        super(idMap, nchar, numStates);
        int ntaxa = super.ntaxa();
        alignment = new int[ntaxa][nchar];
    }

    public Alignment(Map<String, Integer> idMap, int nchar, SequenceType sequenceType) {
        super(idMap, nchar, sequenceType);
        int ntaxa = super.ntaxa();
        alignment = new int[ntaxa][nchar];
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

        if (state < 0 || state > sequenceType.getStateCount()-1) {
            if (ambiguous && state < sequenceType.getStateCount())
                System.err.println("There is ambiguous state " + state + " = " + sequenceType.getState(state));
            else
                throw new IllegalArgumentException("Tried to set a state outside of the range! state = " + state);
        }
        alignment[taxon][position] = state;
    }

    public int getState(int taxon, int position) {
        return alignment[taxon][position];
    }

    public int n() {
        return alignment.length;
    }

    public int L() {
        return alignment[0].length;
    }

    public int getSiteCount() {
        return L();
    }

    @Override
    public boolean hasParts() {
        return false;
    }

    public String toJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n(); i++) {
            builder.append("  ").append(reverseMap.get(i));
            builder.append(" = ").append(Arrays.toString(alignment[i]));
//            if (i < n()-1)
            builder.append(",");
            builder.append("\n");
        }
        builder.append("  nchar = ").append(nchar);
        builder.append(", ntax = ").append(super.ntaxa()).append("\n");
        builder.append("}");
        return builder.toString();
    }

    public String getSequence(int taxonIndex) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < alignment[taxonIndex].length; j++) {
            if (sequenceType == null) // TODO BINARY
                builder.append(getBinaryChar(alignment[taxonIndex][j]));
            else
                builder.append(sequenceType.getState(alignment[taxonIndex][j]));
        }
        return builder.toString();
    }

    // TODO BINARY
    private char getBinaryChar(int state) {
        if (numStates != 2) throw new IllegalArgumentException("Please use SequenceType !");
        return (char)('0' + state);
    }
}
