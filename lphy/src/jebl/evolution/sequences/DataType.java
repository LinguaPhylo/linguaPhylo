package jebl.evolution.sequences;


import java.util.List;

/**
 * more data types.
 * @author Walter Xie
 */
public abstract class DataType implements SequenceType {

    /**
     * @param dataTypeName  keywords in Nexus or data type descriptions
     */
    public static SequenceType getDataType(String dataTypeName) {
        // change to no space, all lower case
        switch (dataTypeName.trim().toLowerCase()) {
            case "rna":
            case "dna":
            case "nucleotide":
                return NUCLEOTIDE;
            case "aminoacid":
            case "protein":
                return AMINO_ACID;
            case "binary":
                return BINARY;
            case "standard":
            case "continuous": // TODO need to check
                return STANDARD; // TODO nrOfState = symbols.length();
            default:
                throw new UnsupportedOperationException(dataTypeName);
        }
    }

    /**
     * @param numStates  Not counting ambiguous characters
     */
    public static SequenceType guessSequenceType(int numStates) {
        switch (numStates) {
            case 2: return BINARY;
            case 4: return NUCLEOTIDE; // make it default ?
            case 20: return AMINO_ACID;
            // TODO Codon.INSTANCE;
            default: return STANDARD; //TODO Standard(nrOfState)?
        }
    }

    public static boolean isSame(SequenceType type1, SequenceType type2) {
        return type1.getName().equals(type2.getName());
    }


    public static final SequenceType BINARY = new SequenceType() {

        @Override
        public int getStateCount() {
            return Binary.getStateCount();
        }

        @Override
        public List<? extends State> getStates() {
            return Binary.getStates();
        }

        @Override
        public int getCanonicalStateCount() {
            return Binary.getCanonicalStateCount();
        }

        @Override
        public List<? extends State> getCanonicalStates() {
            return Binary.getCanonicalStates();
        }

        @Override
        public State getState(String code) {
            return Binary.getState(code);
        }

        @Override
        public State getState(char code) {
            return Binary.getState(code);
        }

        @Override
        public int getCodeLength() {
            return 1;
        }

        @Override
        public State getState(int index) {
            return Binary.getState(index);
        }

        @Override
        public State getUnknownState() {
            return Binary.getUnknownState();
        }

        @Override
        public State getGapState() {
            return Binary.getGapState();
        }

        @Override
        public boolean isUnknown(State state) {
            return Binary.isUnknown(state);
        }

        @Override
        public boolean isGap(State state) {
            return Binary.isGap(state);
        }

        @Override
        public String getName() {
            return Binary.NAME;
        }

        @Override
        public String getNexusDataType() {
            return Binary.NAME;
        }

        @Override
        public State[] toStateArray(String sequenceString) {
            return Binary.toStateArray(sequenceString);
        }

        @Override
        public State[] toStateArray(byte[] indexArray) {
            return Binary.toStateArray(indexArray);
        }
    };


    public static final SequenceType STANDARD = new SequenceType() {

        @Override
        public int getStateCount() {
            return 0;
        }

        @Override
        public List<? extends State> getStates() {
            return null;
        }

        @Override
        public int getCanonicalStateCount() {
            return 0;
        }

        @Override
        public List<? extends State> getCanonicalStates() {
            return null;
        }

        @Override
        public State getState(String code) {
            return null;
        }

        @Override
        public State getState(char code) {
            return null;
        }

        @Override
        public int getCodeLength() {
            return 0;
        }

        @Override
        public State getState(int index) {
            return null;
        }

        @Override
        public State getUnknownState() {
            return null;
        }

        @Override
        public State getGapState() {
            return null;
        }

        @Override
        public boolean isUnknown(State state) {
            return false;
        }

        @Override
        public boolean isGap(State state) {
            return false;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getNexusDataType() {
            return null;
        }

        @Override
        public State[] toStateArray(String sequenceString) {
            return new State[0];
        }

        @Override
        public State[] toStateArray(byte[] indexArray) {
            return new State[0];
        }
    };
}
