package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.Taxon;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class SimpleAlignment extends AbstractAlignment {

    int[][] alignment;

    // for simulators, and datatype not available
    @Deprecated
    public SimpleAlignment(Map<String, Integer> idMap, int nchar, int numStates) {
        super(idMap, nchar, numStates);
        alignment = new int[ntaxa()][nchar];
    }

    public SimpleAlignment(Map<String, Integer> idMap, int nchar, SequenceType sequenceType) {
        super(idMap, nchar, sequenceType);
        alignment = new int[ntaxa()][nchar];
    }

    public SimpleAlignment(Taxon[] taxa, int nchar, SequenceType sequenceType) {
        super(taxa, nchar, sequenceType);
        this.alignment = new int[ntaxa()][nchar];
    }

    public SimpleAlignment(int nchar, AbstractAlignment source) {
        super(nchar, source);
        alignment = new int[ntaxa()][nchar];
    }

    /**
     * Set states to {@link #alignment}.
     * @param taxon      the index of taxon in the 1st dimension of {@link #alignment}.
     * @param position   the site position in the 2nd dimension of {@link #alignment}.
     * @param state      the state in integer
     */
    public void setState(int taxon, int position, int state) {
        // numStates = sequenceType.getCanonicalStateCount() < getStateCount()
        if ( state < 0 || ( (sequenceType == null &&  state > numStates) ||
                (sequenceType != null && state > sequenceType.getStateCount()-1) ) )
            throw new IllegalArgumentException("Tried to set a state outside of the range [0, " +
                    (sequenceType == null ? numStates : sequenceType.getStateCount()-1) + "] ! state = " + state);

        alignment[taxon][position] = state;
    }

    public void setState(String taxon, int position, int state) {
        setState(idMap.get(taxon), position, state);
    }

    @Override
    public int getState(int taxon, int position) {
        return alignment[taxon][position];
    }

    @Override
    public boolean hasParts() {
        return false;
    }

    public String toJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < ntaxa(); i++) {
            builder.append("  ").append(reverseMap.get(i));
            builder.append(" = ").append(Arrays.toString(alignment[i]));
//            if (i < n()-1)
            builder.append(",");
            builder.append("\n");
        }
        builder.append("  nchar = ").append(nchar);
        builder.append(", ntax = ").append(super.ntaxa());
        if (taxonMap != null)
            builder.append(",\n").append("  ageMap = ").append(taxonMap.toString());
        builder.append("\n").append("}");
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
