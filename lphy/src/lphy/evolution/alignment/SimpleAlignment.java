package lphy.evolution.alignment;

import lphy.evolution.DataFrame;
import lphy.evolution.alignment.datatype.DataType;
import lphy.evolution.traits.CharSetBlock;

import java.util.*;

/**
 * @author Walter Xie
 */
public class SimpleAlignment extends Alignment {

    DataType dataType;
    Map<String, List<CharSetBlock>> charsetMap; // TODO

    public SimpleAlignment(int ntaxa, int nchar, Map<String, Integer> idMap, DataType dataType) {
        super(ntaxa, nchar, idMap);
        this.dataType = dataType;
        super.numStates = dataType.getStateCount();
    }

    public DataType getDataType() {
        return dataType;
    }

    //*** fill in parts, int[][] alignment will treat as parent alignment ***//

    public void fillinParts(Map<String, List<CharSetBlock>> charsetMap) {
        assert charsetMap != null;
        this.charsetMap = charsetMap;

        // sort names
        SortedSet<String> nameSet = new TreeSet<>(charsetMap.keySet());

        partNames = nameSet.toArray(String[]::new);
        partNChar = new Integer[partNames.length];
        parts = new DataFrame[partNames.length];
        // by name
        int tot = 0;
        for (int i = 0; i < partNames.length; i++) {
            String pN = partNames[i];
            List<CharSetBlock> charSetBlocks = charsetMap.get(pN);
            partNChar[i] = getFilteredNChar(charSetBlocks, nchar);
            tot += partNChar[i];
            parts[i] = new DataFrame(ntaxa, partNChar[i]);
        }
//        assert tot == nchar; // Cannot, sometime there are extra options
    }

    @Override
    public String toJSON() {
        if (hasParts())
            return "TODO";
        return super.toJSON();
    }


    //*** TODO It seems not need CharSetAlignment ***//

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

    private int[] filter(List<CharSetBlock> charSetBlocks, int[] parentSeq) {
        List<Integer> filtered = new ArrayList<>();
        for (CharSetBlock block : charSetBlocks) {
            int toSite = block.getTo();
            if (toSite <= 0) {
                toSite = parentSeq.length;
            }
            for (int i = block.getFrom(); i <= toSite; i += block.getEvery()) {
                // the -1 comes from the fact that charsets are indexed from 1 whereas strings are indexed from 0
                filtered.add(parentSeq[i - 1]);
            }
        }
        return filtered.stream().mapToInt(i->i).toArray();
    }


}
