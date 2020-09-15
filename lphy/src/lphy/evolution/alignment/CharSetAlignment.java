package lphy.evolution.alignment;

import lphy.evolution.traits.CharSetBlock;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.*;

/**
 * Multiple partitions of {@link Alignment}.
 * The sequences are in {@link lphy.evolution.DataFrame#parts},
 * but here has no <code>int[][] alignment</code>.
 * @author Walter Xie
 */
public class CharSetAlignment extends AbstractAlignment {

//    Map<String, List<CharSetBlock>> charsetMap;

    /**
     * Multiple partitions. The sequences <code>int[][]</code> will be stored in each of parts,
     * <code>DataFrame[] parts = Alignment[]</code>. Here has no <code>int[][] alignment</code>.
     * @param charsetMap      key is part (charset) name, value is the list of {@link CharSetBlock}.
     * @param partNames       if not null, then only choose these names from <code>charsetMap.
     * @param parentAlignment parent alignment before partitioning.
     */
    public CharSetAlignment(final Map<String, List<CharSetBlock>> charsetMap, String[] partNames,
                            final Alignment parentAlignment) {
        // init alignment from parent
        super(parentAlignment);

        initParts(charsetMap, partNames);
        fillinParts(charsetMap, parentAlignment);
    }

    protected void initParts(final Map<String, List<CharSetBlock>> charsetMap, String[] partNames) {
        assert charsetMap != null;
//        this.charsetMap = charsetMap;

        // sort names
        SortedSet<String> nameSet = new TreeSet<>();

        if ( partNames != null ) {
            for (String key : charsetMap.keySet()) {
                if (Arrays.stream(partNames).anyMatch(key::equalsIgnoreCase))
                    nameSet.add(key); // add key from charsetMap, not from String[] partNames
            }
        } else {
            nameSet.addAll(charsetMap.keySet());
        }

        if (nameSet.size() < 2)
            throw new IllegalArgumentException("Cannot create multi-partition, size = " + nameSet.size());

        this.partNames = nameSet.toArray(new String[0]);
        partNChar = new Integer[this.partNames.length];
        parts = new Alignment[this.partNames.length];
    }

    //*** DataFrame[] parts are Alignment with sequences ***//

    protected void fillinParts(Map<String, List<CharSetBlock>> charsetMap, final Alignment parentAlignment) {
        // by name
        int tot = 0;
        for (int i = 0; i < this.partNames.length; i++) {
            String pN = partNames[i];

            List<CharSetBlock> charSetBlocks = charsetMap.get(pN);

            partNChar[i] = getFilteredNChar(charSetBlocks, this.nchar);
            tot += partNChar[i];
            parts[i] = new Alignment(this.ntaxa, partNChar[i], this.idMap, this.sequenceType);

            // fill filtered seqs
            fillSeqsInParts(charSetBlocks, (Alignment) parts[i], parentAlignment);
        }

        // sometime there are extra charsets
        if (tot != this.nchar)
            throw new IllegalArgumentException("The sum of nchar in each part " + tot +
                    " != " + nchar + " the length of parent alignment !\n" +
                    Arrays.toString(partNames) + "\n" + Arrays.toString(partNChar));

    }

    private int getFilteredNChar(List<CharSetBlock> charSetBlocks, int length) {
        int s = 0;
        for (CharSetBlock block : charSetBlocks) {
            int toSite = block.getTo();
            if (toSite <= 0) {
                toSite = length;
            }
            for (int i = block.getFrom(); i <= toSite; i += block.getEvery()) {
                s++;
            }
        }
        return s;
    }

    private void fillSeqsInParts(List<CharSetBlock> charSetBlocks, Alignment part,
                                 final Alignment parentAlignment) {
        for (int t=0; t < this.ntaxa; t++) {
            int pos = 0;
            for (CharSetBlock block : charSetBlocks) {
                int toSite = block.getTo();
                if (toSite <= 0) {
                    toSite = this.nchar;
                }
                for (int i = block.getFrom(); i <= toSite; i += block.getEvery()) {
                    // the -1 comes from the fact that charsets are indexed from 1 whereas strings are indexed from 0
                    int state = parentAlignment.getState(t,(i - 1));
                    part.setState(t, pos, state, true);
                    pos++;
                }
            }
            assert pos == part.nchar();
        }
    }

    //*** Override ***

    @Override
    public String toJSON() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < partNames.length; i++) {
                Alignment part = (Alignment) parts[i];
                builder.append( partNames[i] + " : " );
                builder.append( part.toJSON() );
                builder.append("\n");
            }
            return builder.toString();
    }

    @Override
    public String toString() {
        if (hasParts()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < partNames.length; i++) {
                builder.append( partNames[i] + " : " );
                builder.append( parts[i].toString() );
                if (i < partNames.length-1) builder.append(", ");
            }
            builder.append("\n");
            return builder.toString();
        }
        return super.toString();
    }

    @Override
    public JComponent getComponent(Value<AbstractAlignment> value) {
        return new JLabel(toString()); // avoid to show, because int[][] alignment = null
    }


    @Override
    public int getState(int taxon, int position) {
        throw new UnsupportedOperationException("in dev");
//        return super.getState(taxon, position); // difficult to map parent alignment position
    }

    @Override
    public void setState(int taxon, int position, int state, boolean ambiguous) {
        throw new UnsupportedOperationException("in dev");
    }

    @Override
    public int n() {
        return ((Alignment) parts[0]).n();
    }

    @Override
    public int L() {
        return Arrays.stream(parts).mapToInt( a -> ((Alignment) a).L()).sum();
    }

}
