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
        dataTypeMap.put("nucleotide", SequenceType.NUCLEOTIDE);

        dataTypeMap.put("aminoacid", SequenceType.AMINO_ACID);
        dataTypeMap.put("protein", SequenceType.AMINO_ACID);

        dataTypeMap.put(PhasedGenotype.NAME.trim().toLowerCase(), PhasedGenotype.INSTANCE);

        dataTypeMap.put(Binary.NAME.trim().toLowerCase(), Binary.getInstance());
        dataTypeMap.put(Continuous.NAME.trim().toLowerCase(), Continuous.getInstance());
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
        return dataTypeMap.get(dataTypeName.trim().toLowerCase());
    }


    /** TODO
     * for simulations, so no ambiguous.
     * @param numStates  Not counting ambiguous characters
     * @return  {@link SequenceType}, or null if not implemented
     */
    public static SequenceType getDataType(int numStates) {
        switch (numStates) {
            case 2: return Binary.getInstance();
            case 4: return SequenceType.NUCLEOTIDE;
            case 20: return SequenceType.AMINO_ACID;
            // TODO cannot recognise STANDARD, Codon;
            default: return null; //not implemented;
        }
    }

    /** TODO
     * @param numStates  the number of states for Standard Data Type
     */
    public static SequenceType getStandardDataType(int numStates) {
        return new Standard(numStates);
    }


//    public SequenceType getDataType(String dataTypeName) {
//        if (Standard.NAME.equalsIgnoreCase(dataTypeName.trim()))
//            throw new IllegalArgumentException("Standard data type has to be created " +
//                    "given either numStates or stateNames !");
//
//        switch (dataTypeName.trim().toLowerCase()) {
//            case "rna":
//            case "dna":
//            case "nucleotide":
//                return SequenceType.NUCLEOTIDE;
//            case "aminoacid":
//            case "protein":
//                return SequenceType.AMINO_ACID;
//            case PhasedGenotype.NAME:
//                return PhasedGenotype.INSTANCE;
//            case Binary.NAME:
//                return Binary.getInstance();
//            case Continuous.NAME:
//                return Continuous.getInstance();
//            default:
//                throw new UnsupportedOperationException(dataTypeName);
//        }
//    }

} // end of DataType
