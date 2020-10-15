package lphy.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.Taxa;
import lphy.evolution.traits.CharSetBlock;

import java.util.Arrays;
import java.util.List;
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
        // numStates = sequenceType.getCanonicalStateCount() < getStateCount()
        if ( state < 0 || ( (sequenceType == null &&  state > numStates) ||
                (sequenceType != null && state > sequenceType.getStateCount()-1) ) )
            throw new IllegalArgumentException("Tried to set a state outside of the range [0, " +
                    (sequenceType == null ? numStates : sequenceType.getStateCount()-1) + "] ! state = " + state);

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


    //*** charsets ***//
    public static class Utils {

        /**
         * @return a {@link SimpleAlignment} partition with sequences
         * split from the parent alignment by list of {@link CharSetBlock}.
         */
        public static SimpleAlignment getCharSetAlignment(List<CharSetBlock> charSetBlocks,
                                                          final SimpleAlignment parentAlignment) {
            int partNChar = getNCharFromCharSet(charSetBlocks, parentAlignment.nchar());
            // copy parentAlignment except of nchar
            SimpleAlignment part = new SimpleAlignment(partNChar, parentAlignment);
            // fill in sequences
            fillSeqsToPartition(charSetBlocks, part, parentAlignment);
            return part;
        }

        private static int getNCharFromCharSet(List<CharSetBlock> charSetBlocks, int nchar) {
            int s = 0;
            for (CharSetBlock block : charSetBlocks) {
                int toSite = block.getTo();
                if (toSite <= 0)
                    toSite = nchar;

                for (int i = block.getFrom(); i <= toSite; i += block.getEvery())
                    s++;
            }
            return s;
        }

        private static void fillSeqsToPartition(List<CharSetBlock> charSetBlocks, SimpleAlignment partAlignment,
                                                final SimpleAlignment parentAlignment) {
            // partAlignment has the same taxa ordering of parentAlignment
            for (int t = 0; t < parentAlignment.ntaxa(); t++) {
                int pos = 0;
                for (CharSetBlock block : charSetBlocks) {
                    int toSite = block.getTo();
                    if (toSite <= 0) 
                        toSite = parentAlignment.nchar();

                    for (int i = block.getFrom(); i <= toSite; i += block.getEvery()) {
                        // the -1 comes from the fact that charsets are indexed from 1 whereas strings are indexed from 0
                        int state = parentAlignment.getState(t, (i - 1));
                        partAlignment.setState(t, pos, state);
                        pos++;
                    }
                }
                assert pos == partAlignment.nchar();
            }
        }
    }

}
