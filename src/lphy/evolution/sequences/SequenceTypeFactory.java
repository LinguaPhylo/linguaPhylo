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
    public SequenceType getNexusDataType(String dataTypeName) {
        // change to no space, all lower case
        switch (dataTypeName.trim().toLowerCase()) {
            case "rna":
            case "dna":
            case "nucleotide":
                return SequenceType.NUCLEOTIDE;
            case "aminoacid":
            case "protein":
                return SequenceType.AMINO_ACID;
//            case "binary":
//                return BINARY;
//            case "standard":
            case "continuous":
                return Continuous.getInstance();
            default:
                throw new UnsupportedOperationException(dataTypeName);
        }
    }

    /**
     * @param numStates  Not counting ambiguous characters
     * @return  {@link SequenceType}, or null if not implemented
     */
    public SequenceType guessSequenceType(int numStates) {
        switch (numStates) {
//            case 2: return BINARY;
            case 4: return SequenceType.NUCLEOTIDE;
            case 20: return SequenceType.AMINO_ACID;
            // TODO BINARY STANDARD, Codon;
            default: return null; //not implemented;
        }
    }


} // end of DataType
