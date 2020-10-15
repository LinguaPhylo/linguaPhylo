package lphy.evolution.alignment;

import lphy.evolution.traits.CharSetBlock;

import java.util.List;

/**
 * @author Walter Xie
 */
public final class AlignmentUtils {
    
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
