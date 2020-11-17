package lphy.evolution.sequences;


import jebl.evolution.sequences.SequenceType;

/**
 * Factory Pattern to create objects of {@link SequenceType}.
 * @author Walter Xie
 */
public class SequenceTypeFactory {

//    public static final SequenceType BINARY = Binary.getInstance();

    /**
     * @param dataTypeName  keywords in Nexus or data type descriptions
     */
    public SequenceType getDataType(String dataTypeName, int numStates) {
        if ( dataTypeName.trim().equalsIgnoreCase(Standard.NAME) )
            return new Standard(numStates);

        return getDataType(dataTypeName);
    }

    public SequenceType getDataType(String dataTypeName) {
        switch (dataTypeName.trim().toLowerCase()) {
            case "rna":
            case "dna":
            case "nucleotide":
                return SequenceType.NUCLEOTIDE;
            case "aminoacid":
            case "protein":
                return SequenceType.AMINO_ACID;
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
