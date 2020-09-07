package lphy.evolution.sequences;


import jebl.evolution.sequences.SequenceType;

/**
 * suppose to contain more data types not available from {@link SequenceType}.
 * @author Walter Xie
 */
public abstract class DataType implements SequenceType {

    /**
     * @param dataTypeName  keywords in Nexus or data type descriptions
     */
    public static SequenceType getNexusDataType(String dataTypeName) {
        // change to no space, all lower case
        switch (dataTypeName.trim().toLowerCase()) {
            case "rna":
            case "dna":
            case "nucleotide":
                return NUCLEOTIDE;
            case "aminoacid":
            case "protein":
                return AMINO_ACID;
//            case "binary":
//                return BINARY;
//            case "standard":
//            case "continuous": // TODO need to check
//                return STANDARD; // TODO ? nrOfState = symbols.length()
            default:
                throw new UnsupportedOperationException(dataTypeName);
        }
    }

    /**
     * @param numStates  Not counting ambiguous characters
     * @return  {@link SequenceType}, or null if not implemented
     */
    public static SequenceType guessSequenceType(int numStates) {
        switch (numStates) {
//            case 2: return BINARY;
            case 4: return NUCLEOTIDE;
            case 20: return AMINO_ACID;
            // TODO BINARY STANDARD, Codon;
            default: return null; //not implemented;
        }
    }

    public static boolean isSame(SequenceType type1, SequenceType type2) {
        return type1.getName().equals(type2.getName());
    }


} // end of DataType
