package lphy.evolution.alignment;

import lphy.evolution.alignment.datatype.DataType;

import java.util.Map;

/**
 * TODO 1) move datatype up?
 * @author Walter Xie
 */
public class SimpleAlignment extends Alignment {

    DataType dataType;

    public SimpleAlignment(int ntaxa, int nchar, Map<String, Integer> idMap, DataType dataType) {
        super(ntaxa, nchar, idMap);
        this.dataType = dataType;
        super.numStates = dataType.getStateCount();
    }

    public SimpleAlignment() { }

    public DataType getDataType() {
        return dataType;
    }


    @Override
    public void setState(int taxon, int position, int state) {
        if (state < 0 || state > numStates-1) {
            if (state > dataType.getAmbiguousStateCount())
                throw new IllegalArgumentException("Tried to set a state outside of the range! state = " + state);
            else
                System.err.println("There is ambiguous state " + state + " = " + dataType.getChar(state));
        }
        alignment[taxon][position] = state;
    }

    @Override
    public String getSequence(int taxonIndex) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < alignment[taxonIndex].length; j++) {
            builder.append(dataType.getChar(alignment[taxonIndex][j]));
        }
        return builder.toString();
    }

}
