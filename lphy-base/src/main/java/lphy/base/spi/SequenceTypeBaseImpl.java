package lphy.base.spi;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.datatype.Binary;
import lphy.base.evolution.datatype.Continuous;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link SequenceType} to extend.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class SequenceTypeBaseImpl implements SequenceTypeExtension {

    @Override
    public Map<String, ? extends SequenceType> declareSequenceTypes() {
        Map<String, SequenceType> dataTypeMap = new ConcurrentHashMap<>();
        dataTypeMap.put("rna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put("dna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put(sanitise(SequenceType.NUCLEOTIDE.getName()), SequenceType.NUCLEOTIDE); // nucleotide

        dataTypeMap.put(sanitise(SequenceType.AMINO_ACID.getName()), SequenceType.AMINO_ACID); // aminoacid
        dataTypeMap.put("protein", SequenceType.AMINO_ACID);

        dataTypeMap.put(sanitise(Binary.NAME), Binary.getInstance());
        dataTypeMap.put(sanitise(Continuous.NAME), Continuous.getInstance());
        return dataTypeMap;
    }

    /**TODO private or protected?
     * LPhy sequence types {@link SequenceType}
     */
    protected static Map<String, SequenceType> dataTypeMap;

    /**
     * Required by ServiceLoader.
     */
    public SequenceTypeBaseImpl() {
        if (dataTypeMap == null)
            dataTypeMap = new ConcurrentHashMap<>();
    }

    /**
     * @param dataTypeName
     * @return   a registered data type, but not Standard data type.
    //     * @see Standard
     */
    public static SequenceType getDataType(String dataTypeName) {
        if (dataTypeMap == null) return null;
        return dataTypeMap.get(sanitise(dataTypeName));
    }

    public static List<SequenceType> getDataTypeList() {
        if (dataTypeMap == null) return new ArrayList<>();
        return dataTypeMap.values().stream().toList();
    }

    /**
     * @param name
     * @return  trimmed lower case
     */
    public static String sanitise(String name) {
        return name.trim().toLowerCase();
    }

    @Override
    public Set<SequenceType> getSequenceTypes() {
        return new HashSet<>(dataTypeMap.values());
    }

    @Override
    public void register() {
        // sequence types
        Map<String, ? extends SequenceType> newTypes = declareSequenceTypes();

        addSequenceTypes(newTypes, dataTypeMap, "LPhy standard sequence types : ");
    }

    public String getExtensionName() {
        return "LPhy standard sequence types";
    }

    //    /**
//     * @param sequenceType
//     * @return true if it is {@link Standard} data type. Ignore case
//     */
//    public boolean isStandardDataType(SequenceType sequenceType) {
//        return sequenceType != null && sequenceType.getName().equalsIgnoreCase(Standard.NAME);
//    }
}
