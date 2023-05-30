package lphy.core.spi;


import jebl.evolution.sequences.SequenceType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to hold {@code Map<String, ? extends {@link SequenceType}> dataTypeMap}.
 *
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
//     * @see Standard
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

//    /**
//     * @param sequenceType
//     * @return true if it is {@link Standard} data type. Ignore case
//     */
//    public boolean isStandardDataType(SequenceType sequenceType) {
//        return sequenceType != null && sequenceType.getName().equalsIgnoreCase(Standard.NAME);
//    }


} // end of DataType
