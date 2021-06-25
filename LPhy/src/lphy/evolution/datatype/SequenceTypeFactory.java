package lphy.evolution.datatype;


import jebl.evolution.sequences.SequenceType;
import lphy.app.ColourPalette;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registry Pattern to create instances of {@link SequenceType},
 * and register data types.
 * @author Walter Xie
 */
public class SequenceTypeFactory {

    private Map<String, ? extends SequenceType> dataTypeMap;

    public static SequenceTypeFactory INSTANCE = new SequenceTypeFactory();
    // register data types here
    private SequenceTypeFactory() { }

    public void setDataTypeMap(Map<String, ? extends SequenceType> dataTypeMap) {
        this.dataTypeMap = dataTypeMap;
    }

    public Set<SequenceType> getAllDataTypes() {
        return new HashSet<>(dataTypeMap.values());
    }

    /**
     * @param dataTypeName
     * @return   a registered data type, but not Standard data type.
     * @see Standard
     */
    public SequenceType getDataType(String dataTypeName) {
        return dataTypeMap.get(sanitise(dataTypeName));
    }

    /**
     * @param name
     * @return  trimmed lower case
     */
    public static String sanitise(String name) {
        return name.trim().toLowerCase();
    }

    /**
     * @param sequenceType
     * @return true if it is {@link Standard} data type. Ignore case
     */
    public boolean isStandardDataType(SequenceType sequenceType) {
        return sequenceType != null && sequenceType.getName().equalsIgnoreCase(Standard.NAME);
    }


    //*** Colours ***//

    // TODO
    /**
     * @param sequenceType
     * @return  a {@link Color} array to visualise sequences, including uncertain states
     */
    public Color[] getCanonicalStateColours(SequenceType sequenceType) {
        // extra 2 colours for UNKNOWN_STATE, GAP_STATE
        if ( sequenceType.getCanonicalStateCount() <=  2 && sequenceType.getStateCount() <= 4 )
            return ColourPalette.getTwoPlusOne();
        else if ( sequenceType.getCanonicalStateCount() <=  4 ) // DNA or traits
            return ColourPalette.getFourPlusOne();
        else if ( sequenceType.getCanonicalStateCount() <=  20 ) // AMINO_ACID
            return ColourPalette.getTwentyPlusOne();
        else throw new IllegalArgumentException("Cannot choose colours given data type " +
                    sequenceType + " and numStates " + sequenceType.getCanonicalStateCount() + " !");
    }
    // TODO how to colour uncertain states ?
    /**
     * @return  state, if 0 <= state < numStates (no ambiguous),
     *          otherwise return numStates which is the last index
     *          in colours always for ambiguous state.
     */
//    public static int getColourIndex(int state, SequenceType sequenceType) {
//        if (sequenceType == null)
//            throw new IllegalArgumentException("SequenceType is required !");
//
//        if (sequenceType.getName().equals(Binary.NAME) && state > 1 )
//            return 2;
//        if (sequenceType.getName().equals(SequenceType.NUCLEOTIDE.getName()) && state > 3)
//            return 4;
//        else if (sequenceType.getName().equals(SequenceType.AMINO_ACID.getName()) && state > 19) // no ambiguous
//            //TODO why jebl make AMINO_ACID 22 ?
//            return 20; // the last extra is always for ambiguous
//        return state;
//    }

//    public static Color getColourByDataType(int state, SequenceType sequenceType) {
//    }


} // end of DataType
