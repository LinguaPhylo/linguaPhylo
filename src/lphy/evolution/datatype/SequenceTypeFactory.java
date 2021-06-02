package lphy.evolution.datatype;


import jebl.evolution.sequences.SequenceType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry Pattern to create instances of {@link SequenceType},
 * and register data types.
 * @author Walter Xie
 */
public class SequenceTypeFactory {

    public static final Map<String, SequenceType> dataTypeMap = new ConcurrentHashMap<>();

    static {
        dataTypeMap.put("rna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put("dna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put(sanitise(SequenceType.NUCLEOTIDE.getName()), SequenceType.NUCLEOTIDE); // nucleotide

        dataTypeMap.put(sanitise(SequenceType.AMINO_ACID.getName()), SequenceType.AMINO_ACID); // aminoacid
        dataTypeMap.put("protein", SequenceType.AMINO_ACID);

        dataTypeMap.put(sanitise(PhasedGenotype.NAME), PhasedGenotype.INSTANCE);

        dataTypeMap.put(sanitise(Binary.NAME), Binary.getInstance());
        dataTypeMap.put(sanitise(Continuous.NAME), Continuous.getInstance());
    }

    private static String sanitise(String name) {
        return name.trim().toLowerCase();
    }

    // register data types here
    private SequenceTypeFactory() { }

    public static Set<SequenceType> getAllDataTypes() {
        return new HashSet<>(dataTypeMap.values());
    }

    /**
     * @param dataTypeName
     * @return   a registered data type, but not Standard data type.
     * @see Standard
     */
    public static SequenceType getDataType(String dataTypeName) {
        return dataTypeMap.get(sanitise(dataTypeName));
    }

} // end of DataType
