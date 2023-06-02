package lphy.base.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.core.exception.LoggerUtils;

import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Walter Xie
 */
public final class AlignmentUtils {

    public final static String ALIGNMENT_PARAM_NAME = "alignment";
    public final static String IGNORE_UNKNOWN_PARAM_NAME = "ignoreUnknown";

    /**
     * @param aSite            states as an int array
     * @param ignoreUnknown    ignore unknown or gap
     * @param sequenceType     {@link  SequenceType}
     * @return      true if the site is an invariable site
     */
    public static boolean isInvarSite(final int[] aSite, boolean ignoreUnknown, SequenceType sequenceType) {
        int[] unique = Arrays.stream(aSite).distinct().toArray();
        if (unique == null || unique.length == 0)
            throw new IllegalArgumentException("Illegal array cannot find unique int : " + Arrays.toString(aSite));
        // all same
        if (unique.length == 1) return true;
        // all same after ignore unknown state ?
        if (ignoreUnknown) {
            return unique.length == 2 &&
                    IntStream.of(unique).
                            anyMatch(x -> x == sequenceType.getUnknownState().getIndex() ||
                                    x == sequenceType.getGapState().getIndex() );
        }
        return false;
    }

    /**
     * @param alignment  {@link SimpleAlignment} only containing the constant sites.
     * @param ignoreUnknown    ignore unknown or gap
     * @return   a counter of constant sites,
     *           where the key is one of the sorted canonical states,
     *           and the value is how many constant sites at that state.
     *           If a canonical state does not exist in the alignment,
     *           then it will be not in the key set.
     *           {@link NavigableMap#firstKey()} is the smallest recorded canonical state in number,
     *           {@link NavigableMap#lastKey()} is the largest recorded canonical state in number.
     */
    public static NavigableMap<Integer, Integer> getConstantSiteWeights(
            final SimpleAlignment alignment, boolean ignoreUnknown) {
        SequenceType sequenceType = alignment.getSequenceType();

        int miss = 0;
        NavigableMap<Integer, Integer> counter = new TreeMap<>();
        for (int j = 0; j < alignment.nchar(); j++) {
            int[] aSite = new int[alignment.ntaxa()];
            for (int i = 0; i < alignment.ntaxa(); i++) {
                aSite[i] = alignment.getState(i, j);
            }
            // find the state
            OptionalInt state = IntStream.of(aSite).
                    filter(x -> x != sequenceType.getUnknownState().getIndex() &&
                            x != sequenceType.getGapState().getIndex() ).
                    findFirst();
            if (state.isPresent())
                counter.merge(state.getAsInt(), 1, Integer::sum);
            else
                miss++;
        }
        if (miss > 0)
            LoggerUtils.log.severe("Error: the constant alignment contains " + miss +
                    " non-constant sites" + (ignoreUnknown ? ", when ignoring unknown state or gap !" : " !"));
        return counter;
    }

    /**
     * @param alignment  {@link SimpleAlignment} storing states as integers.
     * @return   a counter of counting constant sites, where the key is the sorted state,
     *           and the value is the count of the constant site on that state.
     *           If any states not existing in the key, then assume the count is 0.
     *           {@link NavigableMap#firstKey()} is the smallest recorded state in number,
     *           {@link NavigableMap#lastKey()} is the largest recorded state in number.
     */
    @Deprecated
    public static NavigableMap<Integer, Integer> countConstantSites(final SimpleAlignment alignment) {
        NavigableMap<Integer, Integer> counter = new TreeMap<>();

        // index is the site index, if constant site, the value is a state
        int[] mark = Objects.requireNonNull(alignment).getConstantSitesMark();
        for (int m : mark) {
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
