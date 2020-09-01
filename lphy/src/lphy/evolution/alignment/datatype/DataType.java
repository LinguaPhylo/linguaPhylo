package lphy.evolution.alignment.datatype;


/**
 * Base class for sequence data types.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: DataType.java,v 1.13 2005/05/24 20:25:56 rambaut Exp $
 */
public abstract class DataType {
//    private static final long serialVersionUID = 2L;

    public static final int NUCLEOTIDES = 0;
    public static final int AMINO_ACIDS = 1;
    public static final int CODONS = 2;
    public static final int TWO_STATES = 3;
    public static final int GENERAL = 4;
    public static final int COVARION = 5;
    public static final int MICRO_SAT = 6;

    public static final int P2PTYPE = 7;
    public static final int CONTINUOUS = 8;

    public static final char UNKNOWN_CHARACTER = '?';
    public static final char GAP_CHARACTER = '-';

    protected int stateCount;
    protected int ambiguousStateCount;


    /**
     * guess data type suitable for a given sequence
     *
     * @param sequence a string of symbols representing a molecular sequence of unknown data type.
     * @return suitable DataType object
     */
    public static DataType guessDataType(String sequence) {
        // count A, C, G, T, U, N
        long numNucs = 0;
        long numChars = 0;
        long numBins = 0;
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            int s = Nucleotides.INSTANCE.getState(c);

            if (s != Nucleotides.UNKNOWN_STATE && s != Nucleotides.GAP_STATE) {
                numNucs++;
            }

            if (c != '-' && c != '?') {
                numChars++;
            }

            if (c == '0' || c == '1') numBins++;
        }

        if (numChars == 0) {
            // if empty or only gaps and ? then assume nucleotide
            return Nucleotides.INSTANCE;
        }

        // more than 85 % frequency advocates nucleotide data
        if ((double) numNucs / (double) numChars > 0.85) {
            return Nucleotides.INSTANCE;
        } else if ((double) numBins / (double) numChars > 0.2) {
            return TwoStates.INSTANCE;
        } else {
            return AminoAcids.INSTANCE;
        }
    }

    public static DataType getDataType(String dataTypeName) {
        switch (dataTypeName) {
            case "rna":
            case "dna":
            case "nucleotide":
                return Nucleotides.INSTANCE;
            case "aminoacid":
            case "protein":
                return AminoAcids.INSTANCE;
            case "binary":
                return TwoStates.INSTANCE;
//            case "standard":
            default:
                throw new UnsupportedOperationException(dataTypeName);
//                return Standard(nrOfState); // TODO nrOfState = symbols.length();
        }

    }

    /**
     * @param numStates  Not counting ambiguous characters
     * @return
     */
    public static DataType guessDataType(int numStates) {
        switch (numStates) {
            case 2: return TwoStates.INSTANCE;
            case 4: return Nucleotides.INSTANCE;
            case 20: return AminoAcids.INSTANCE;
            default: return Standard.INSTANCE; //TODO Standard(nrOfState)?
        }
    }


    /**
     * return the set of valid chars if they are defined, if not defined then return null
     * cannot use stateCount and loop, because some data types stateCount is dynamic.
     */
    public abstract char[] getValidChars();

    /**
     * Get number of unique states
     *
     * @return number of unique states
     */
    public int getStateCount() {
        return stateCount;
    }

    /**
     * Get number of states including ambiguous states
     *
     * @return number of ambiguous states
     */
    public int getAmbiguousStateCount() {
        return ambiguousStateCount;
    }

    /**
     * Get state corresponding to a character
     *
     * @param code state code
     * @return state
     */
    public int getState(String code) {
        return getState(code.charAt(0));
    }

    /**
     * Get state corresponding to a character
     *
     * @param c character
     * @return state
     */
    public int getState(char c) {
        return (int) c - 'A';
    }

    /**
     * Get state corresponding to an unknown
     *
     * @return state
     */
    public int getUnknownState() {
        return stateCount;
    }

    /**
     * Get state corresponding to a gap
     *
     * @return state
     */
    public int getGapState() {
        return stateCount + 1;
    }

    /**
     * Get character corresponding to a given state
     *
     * @param state state
     *              <p/>
     *              return corresponding character
     */
    public char getChar(int state) {
        return (char) (state + 'A');
    }

    /**
     * Get a string code corresponding to a given state. By default this
     * calls getChar but overriding classes may return multicharacter codes.
     *
     * @param state state
     *              <p/>
     *              return corresponding code
     */
    public String getCode(int state) {
        return String.valueOf(getChar(state));
    }

    /**
     * Get triplet string corresponding to a given state
     *
     * @param state state
     *              <p/>
     *              return corresponding triplet string
     */
    public String getTriplet(int state) {
        return " " + getChar(state) + " ";
    }

    /**
     * returns an array containing the non-ambiguous states that this state represents.
     */
    public int[] getStates(int state) {

        int[] states;
        if (!isAmbiguousState(state)) {
            states = new int[1];
            states[0] = state;
        } else {
            states = new int[stateCount];
            for (int i = 0; i < stateCount; i++) {
                states[i] = i;
            }
        }

        return states;
    }

    /**
     * returns an array containing the non-ambiguous states that this state represents.
     */
    public boolean[] getStateSet(int state) {

        boolean[] stateSet = new boolean[stateCount];
        if (!isAmbiguousState(state)) {
            for (int i = 0; i < stateCount; i++) {
                stateSet[i] = false;
            }

            stateSet[state] = true;
        } else {
            for (int i = 0; i < stateCount; i++) {
                stateSet[i] = true;
            }
        }

        return stateSet;
    }

    public String toString() {
        return getDescription();
    }

    /**
     * description of data type
     *
     * @return string describing the data type
     */
    public abstract String getDescription();

    /**
     * type of data type
     *
     * @return integer code for the data type
     */
    public abstract int getType();

    /**
     * @return true if this character is an ambiguous state
     */
    public boolean isAmbiguousChar(char c) {
        return isAmbiguousState(getState(c));
    }

    /**
     * @return true if this character is a gap
     */
    public boolean isUnknownChar(char c) {
        return isUnknownState(getState(c));
    }

    /**
     * @return true if this character is a gap
     */
    public boolean isGapChar(char c) {
        return isGapState(getState(c));
    }

    /**
     * returns true if this state is an ambiguous state.
     */
    public boolean isAmbiguousState(int state) {
        return (state >= stateCount);
    }

    /**
     * @return true if this state is an unknown state
     */
    public boolean isUnknownState(int state) {
        return (state == getUnknownState());
    }

    /**
     * @return true if this state is a gap
     */
    public boolean isGapState(int state) {
        return (state == getGapState());
    }

    public String getName() {
        switch (getType()) {
            case DataType.NUCLEOTIDES:
                return "Nucleotide";
            case DataType.AMINO_ACIDS:
                return "Amino Acid";
            case DataType.CODONS:
                return "Codon";
            case DataType.TWO_STATES:
                return "Binary";
            case DataType.COVARION:
                return "Covarion";
            case DataType.GENERAL:
                return "Discrete Traits";
            case DataType.CONTINUOUS:
                return "Continuous Traits";
            case DataType.MICRO_SAT:
                return "Microsatellite";
            default:
                throw new IllegalArgumentException("Unsupported data type");

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataType)) return false;

        DataType dataType = (DataType) o;

        if (this.getType() != dataType.getType()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getType();
    }


}
