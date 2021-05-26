package lphy.evolution.sequences;


import jebl.evolution.sequences.SequenceType;

/**
 * Factory Pattern to create objects of {@link SequenceType}.
 * @author Walter Xie
 */
public class SequenceTypeFactory {

//    public static final SequenceType BINARY = Binary.getInstance();

    /**
     * @param numStates  the number of states for Standard Data Type
     */
    public SequenceType getStandardDataType(int numStates) {
        return new Standard(numStates);
    }

    /**
     * Not include Standard data type
     * @param dataTypeName
     * @return
     */
    public SequenceType getDataType(String dataTypeName) {
        if (Standard.NAME.equalsIgnoreCase(dataTypeName.trim()))
            throw new IllegalArgumentException("Standard data type has to be created " +
                    "given either numStates or stateNames !");

        switch (dataTypeName.trim().toLowerCase()) {
            case "rna":
            case "dna":
            case "nucleotide":
                return SequenceType.NUCLEOTIDE;
            case "aminoacid":
            case "protein":
                return SequenceType.AMINO_ACID;
            case "phasedgenotype":
                return PhasedGenotype.INSTANCE;
            case Binary.NAME:
                return Binary.getInstance();
            case Continuous.NAME:
                return Continuous.getInstance();
            default:
                throw new UnsupportedOperationException(dataTypeName);
        }
    }

    /**
     * for simulations, so no ambiguous.
     * @param numStates  Not counting ambiguous characters
     * @return  {@link SequenceType}, or null if not implemented
     */
    public SequenceType getSequenceType(int numStates) {
        switch (numStates) {
            case 2: return Binary.getInstance();
            case 4: return SequenceType.NUCLEOTIDE;
            case 20: return SequenceType.AMINO_ACID;
            // TODO BINARY STANDARD, Codon;
            default: return null; //not implemented;
        }
    }


} // end of DataType
