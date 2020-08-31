package lphy.evolution.alignment;

import lphy.evolution.traits.CharSetBlock;

import java.util.*;

/**
 * @author Walter Xie
 */
public class CharSetAlignment extends SimpleAlignment {

    Map<String, List<CharSetBlock>> charsetMap;


    public CharSetAlignment(final Map<String, List<CharSetBlock>> charsetMap, final SimpleAlignment parentAlignment) {
        this(charsetMap, null, parentAlignment);
    }

    public CharSetAlignment(final Map<String, List<CharSetBlock>> charsetMap, String[] partNames, final SimpleAlignment parentAlignment) {
        this.ntaxa = parentAlignment.ntaxa();
        this.nchar = parentAlignment.nchar();
        this.alignment = new int[ntaxa][nchar]; // but no seqs
        this.idMap = new TreeMap<>(parentAlignment.idMap);
        fillRevMap(idMap);

        this.dataType = parentAlignment.getDataType();

        initParts(charsetMap, partNames);
        fillinParts(charsetMap, parentAlignment);
    }

    protected void initParts(final Map<String, List<CharSetBlock>> charsetMap, String[] partNames) {
        assert charsetMap != null;
        this.charsetMap = charsetMap;

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
        this.partNames = nameSet.toArray(new String[0]);
        partNChar = new Integer[this.partNames.length];
        parts = new SimpleAlignment[this.partNames.length];
    }

    //*** DataFrame[] parts are SimpleAlignment with sequences ***//

    protected void fillinParts(Map<String, List<CharSetBlock>> charsetMap, final SimpleAlignment parentAlignment) {
        // by name
        int tot = 0;
        for (int i = 0; i < this.partNames.length; i++) {
            String pN = partNames[i];

            List<CharSetBlock> charSetBlocks = charsetMap.get(pN);

            partNChar[i] = getFilteredNChar(charSetBlocks, this.nchar);
            tot += partNChar[i];
            parts[i] = new SimpleAlignment(this.ntaxa, partNChar[i], this.idMap, this.dataType);

            // fill filtered seqs
            fillSeqsInParts(charSetBlocks, (SimpleAlignment) parts[i], parentAlignment);
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

    private void fillSeqsInParts(List<CharSetBlock> charSetBlocks, SimpleAlignment part,
                                 final SimpleAlignment parentAlignment) {
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
                    part.setState(t, pos, state);
                    pos++;
                }
            }
            assert pos == part.nchar();
        }
    }

    //*** TODO Override ***//

    @Override
    public String toJSON() {
        if (hasParts())
            return "TODO";
        return super.toJSON();
    }






//    private int[] filter(List<CharSetBlock> charSetBlocks, int[] parentSeq) {
//        List<Integer> filtered = new ArrayList<>();
//        for (CharSetBlock block : charSetBlocks) {
//            int toSite = block.getTo();
//            if (toSite <= 0) {
//                toSite = parentSeq.length;
//            }
//            for (int i = block.getFrom(); i <= toSite; i += block.getEvery()) {
//                // the -1 comes from the fact that charsets are indexed from 1 whereas strings are indexed from 0
//                filtered.add(parentSeq[i - 1]);
//            }
//        }
//        return filtered.stream().mapToInt(i->i).toArray();
//    }



}
