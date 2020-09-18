package lphy.evolution.alignment;

import lphy.evolution.traits.CharSetBlock;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.*;

/**
 * Multiple partitions of {@link SimpleAlignment}.
 * The sequences are stored in {@link lphy.evolution.alignment.CharSetAlignment#partsMap},
 * but here has no <code>int[][] alignment</code>.
 *
 * @author Walter Xie
 */
public class CharSetAlignment extends AbstractAlignment {

    //    Map<String, List<CharSetBlock>> charsetMap;
    protected Map<String, SimpleAlignment> partsMap = null;

    /**
     * Multiple partitions. The sequences <code>int[][]</code> will be stored in each of parts,
     * <code>Map<String, Alignment> partsMap</code>. Here has no <code>int[][] alignment</code>.
     *
     * @param charsetMap      key is part (charset) name, value is the list of {@link CharSetBlock}.
     * @param partNames       if null, import all charsets,
     *                        if not null, then only choose the subset of names from <code>charsetMap</code>.
     * @param parentAlignment parent alignment before partitioning.
     */
    public CharSetAlignment(final Map<String, List<CharSetBlock>> charsetMap, String[] partNames,
                            final SimpleAlignment parentAlignment) {
        // init this alignment from parent
        super(parentAlignment);
        createPartAlignments(charsetMap, partNames, parentAlignment);
    }

    /**
     * Import all charsets in <code>charsetMap</code>.
     * @see #CharSetAlignment(Map, String[], SimpleAlignment)
     */
    public CharSetAlignment(final Map<String, List<CharSetBlock>> charsetMap, final SimpleAlignment parentAlignment) {
        this(charsetMap, null, parentAlignment);
    }

    public String[] getPartNames() {
        return Objects.requireNonNull(partsMap).keySet().toArray(String[]::new);
    }

    public SimpleAlignment getPartAlignment(String partName) {
        SimpleAlignment a = partsMap.get(partName);
        if (a == null)
            throw new IllegalArgumentException("Charset name " + partName + " not exist in CharSetAlignment !");
        return a;
    }

    public SimpleAlignment[] getPartAlignments(String[] partNames) {
        List<SimpleAlignment> alignments = new ArrayList<>();
        for (String name : partNames) {
            alignments.add(getPartAlignment(name));
        }
        return alignments.toArray(SimpleAlignment[]::new);
    }

    // if partNames is null, pull names from charsetMap.
    // init Alignment, and fill in sequences
    protected void createPartAlignments(final Map<String, List<CharSetBlock>> charsetMap,
                                        String[] partNames, final SimpleAlignment parentAlignment) {
        assert charsetMap != null;
//        this.charsetMap = charsetMap;

        // sort names
        SortedSet<String> nameSet = new TreeSet<>();
        if (partNames != null) {
            for (String key : charsetMap.keySet()) {
                if (Arrays.stream(partNames).anyMatch(key::equalsIgnoreCase))
                    nameSet.add(key); // add key from charsetMap, not from String[] partNames
            }
        } else {
            nameSet.addAll(charsetMap.keySet());
        }

        if (nameSet.size() < 2)
            throw new IllegalArgumentException("Cannot create multi-partition, size = " + nameSet.size());

        partsMap = new LinkedHashMap<>();
        // by name
        int tot = 0;
        for (String partName : nameSet) {
            List<CharSetBlock> charSetBlocks = charsetMap.get(partName);

            int partNChar = getFilteredNChar(charSetBlocks, this.nchar);
            tot += partNChar;
            SimpleAlignment part = new SimpleAlignment(this.idMap, partNChar, this.sequenceType);
            // fill in sequences
            fillSeqsInParts(charSetBlocks, part, parentAlignment);

            partsMap.put(partName, part);
        }

        // sometime there are extra charsets
        if (tot != this.nchar)
            System.err.println("Warning: there is extra partition(s) defined in Nexus, " +
                    "where the total nchar in each part " + tot + " != " + nchar +
                    " the length of full alignment !\nPartitions (ntaxa by nchar) : " + partsMap);

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

    private void fillSeqsInParts(List<CharSetBlock> charSetBlocks, SimpleAlignment part,
                                 final SimpleAlignment parentAlignment) {
        // this CharSetAlignment has the same taxa mapping of all parts Alignment
        for (int t = 0; t < this.ntaxa(); t++) {
            int pos = 0;
            for (CharSetBlock block : charSetBlocks) {
                int toSite = block.getTo();
                if (toSite <= 0) {
                    toSite = this.nchar;
                }
                for (int i = block.getFrom(); i <= toSite; i += block.getEvery()) {
                    // the -1 comes from the fact that charsets are indexed from 1 whereas strings are indexed from 0
                    int state = parentAlignment.getState(t, (i - 1));
                    part.setState(t, pos, state);
                    pos++;
                }
            }
            assert pos == part.nchar();
        }
    }

    //*** Override ***

    @Override
    public String toJSON() {
        String[] partNames = getPartNames();
        return toJSON(partNames);
    }

    public String toJSON(String[] partNames) {
        StringBuilder builder = new StringBuilder();
        for (String name : partNames) {
            SimpleAlignment part = getPartAlignment(name);
            if (part == null) throw new IllegalArgumentException("Charset name " + name + " not exist in CharSetAlignment !");
            builder.append(name).append(" : ");
            builder.append(part.toJSON());
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = partsMap.size();
        for (Map.Entry<String, SimpleAlignment> entry : partsMap.entrySet()) {
            builder.append(entry.getKey()).append(" : ");
            SimpleAlignment part = entry.getValue();
            builder.append(part.toString());
            if (i > 1) builder.append(", ");
            i--;
        }
        builder.append("\n");
        return builder.toString();
    }

    @Override
    public JComponent getComponent(Value<AbstractAlignment> value) {
        return new JLabel(toString()); // avoid to show, no int[][] alignment
    }


    @Override
    public int getState(int taxon, int position) {
        throw new UnsupportedOperationException("in dev");
//        return super.getState(taxon, position); // difficult to map parent alignment position
    }

    @Override
    public void setState(int taxon, int position, int state) {
        throw new UnsupportedOperationException("in dev");
    }

    @Override
    public boolean hasParts() {
        return true;
    }
}
