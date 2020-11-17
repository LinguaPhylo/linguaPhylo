package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.Taxa;
import lphy.evolution.sequences.Binary;
import lphy.evolution.sequences.Standard;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class SimpleAlignment extends AbstractAlignment {

    int[][] alignment;

    /**
     * for simulated alignment
     * @see AbstractAlignment
     */
    public SimpleAlignment(Map<String, Integer> idMap, int nchar, SequenceType sequenceType) {
        super(idMap, nchar, sequenceType);
        alignment = new int[ntaxa()][nchar];
    }

    public SimpleAlignment(Taxa taxa, int nchar, SequenceType sequenceType) {
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
        if (sequenceType == null)
            throw new IllegalArgumentException("Please define SequenceType, not numStates !");
        // TODO how to distinguish imported alignment and simulated
        if ( state < 0 ||  state > getStateCount() )
            throw new IllegalArgumentException("Illegal to set a " + sequenceType.getName() +
                    " state outside of the range [0, " + (sequenceType.getStateCount()-1) + "] ! state = " + state);
        alignment[taxon][position] = state;
    }

    public void setState(String taxon, int position, int state) {
        setState(indexOfTaxon(taxon), position, state);
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
            builder.append("  ").append(getTaxonName(i));
            builder.append(" = ").append(Arrays.toString(alignment[i]));
//            if (i < n()-1)
            builder.append(",");
            builder.append("\n");
        }
        builder.append("  nchar = ").append(nchar);
        builder.append(", ntax = ").append(super.ntaxa());
        if (hasAges())
            builder.append(",\n").append("  ages = ").append(Arrays.toString(getAges()));
        builder.append("\n").append("}");
        return builder.toString();
    }

    public String getSequence(int taxonIndex) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < alignment[taxonIndex].length; j++) {
            if (Objects.requireNonNull(sequenceType).getName().equals(Binary.NAME))
                builder.append(getBinaryChar(alignment[taxonIndex][j]));
            else if (sequenceType.getName().equals(Standard.NAME))
                builder.append(alignment[taxonIndex][j]);
            else
                builder.append(sequenceType.getState(alignment[taxonIndex][j]));
        }
        return builder.toString();
    }

    private char getBinaryChar(int state) {
        if (getCanonicalStateCount() != 2)
            throw new IllegalArgumentException("Binary only have 2 states ! state = " + state);
        return (char)('0' + state);
    }

}
