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
     * @param alignment  {@link SimpleAlignment} storing states as integers.
     * @return   a counter of counting constant sites, where the index is the state,
     *           and the value is the count of the constant site on that state.
     *           If any states not existing in the key, then assume the count is 0.
     */
    public static Map<Integer, Integer> countConstantSites(final SimpleAlignment alignment) {
        Map<Integer, Integer> counter = new TreeMap<>();

        // index is the site index, if constant site, the value is a state
        int[] marker = Objects.requireNonNull(alignment).getConstantSitesMarker();
        for (int m : marker) {
            if (m > 0) // -1 for variable site
                counter.merge(m, 1, Integer::sum);
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
