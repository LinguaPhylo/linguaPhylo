package lphy.evolution.alignment;

import lphy.evolution.traits.CharSetBlock;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Walter Xie
 */
public final class AlignmentUtils {

    /**
     * Exclude ambiguous states and gaps.
     * @param alignment  {@link SimpleAlignment} storing states as integers.
     * @return   int[] of counting constant sites, where the index is the state,
     *           and the value is the count of the constant site on that state.
     */
    public static int[] getConstantSites(final SimpleAlignment alignment) {
        final int ntaxa = Objects.requireNonNull(alignment).ntaxa();
        final int nsites = alignment.nchar();
        // Exclude ambiguous states and gaps.
        final int stateCount = alignment.getCanonicalStateCount();

        // index is state
        int[] counter = new int[stateCount];

        boolean isConstant;
        int firstState;
        for (int i = 0; i < nsites; i++) {
            isConstant = true;
            firstState = alignment.getState(0, i);
            for (int t = 1; t < ntaxa; t++) {
                if (alignment.getState(t, i) != firstState) {
                    isConstant = false;
                    break;
                }
            }

            if (isConstant)
                counter[firstState] += 1;
        }
        return counter;
    }
    
    //*** charsets ***//

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
