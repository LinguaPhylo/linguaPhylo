package lphy.base.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import lphy.base.evolution.Taxa;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.MethodInfo;
import lphy.core.model.annotation.TypeInfo;

import java.util.List;
import java.util.Objects;

/**
 * An alignment of discrete character states, where states are integers.
 * @author Alexei Drummond
 * @author Walter Xie
 */
@TypeInfo(description = "An alignment of discrete character states, where states are integers.",
        examples = {"simpleSerialCoalescentNex.lphy","twoPartitionCoalescentNex.lphy"})
public interface Alignment extends Taxa, TaxaCharacterMatrix<Integer> {

    //****** MethodInfo ******//

    @MethodInfo(description = "the taxa of the alignment.", narrativeName = "list of taxa",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            examples = {"twoPartitionCoalescentNex.lphy","https://linguaphylo.github.io/tutorials/time-stamped-data/"})
    default Taxa taxa() {
        return getTaxa();
    }

    @MethodInfo(description = "the number of possible states including ambiguous states in the alignment.")
    default int stateCount() {
        return getStateCount();
    }

    @MethodInfo(description = "the number of canonical states excluding ambiguous states in the alignment.",
            narrativeName = "number of canonical states", examples = {"covidDPG.lphy"})
    default int canonicalStateCount() {
        return getCanonicalStateCount();
    }

    @MethodInfo(description = "the possible states including ambiguous states.")
    default List<? extends State> states() {
        return getStates();
    }

    @MethodInfo(description = "the canonical states excluding ambiguous states.")
    default List<? extends State> canonicalStates() {
        return getCanonicalStates();
    }

    @MethodInfo(description = "get the data type of this alignment.", narrativeName = "data type")
    default SequenceType dataType() {
        return getSequenceType();
    }

    //****** states ******//

    /**
     * Set int states.
     * @param taxon      the index of taxon in the 1st dimension.
     * @param position   the site position in the 2nd dimension.
     * @param state      the state in integer
     */
    void setState(int taxon, int position, Integer state);

    /**
     * @param taxon      the name of the taxon.
     * @param position   the site position.
     * @return  the integer state at the given coordinate of this alignment.
     */
    default Integer getState(String taxon, int position) {
        return getState(indexOfTaxon(taxon), position);
    }

    /**
     * @param taxon      the index of taxon.
     * @param position   the site position.
     * @return  the integer state at the given coordinate of this alignment.
     */
    int getState(int taxon, int position);


    //****** data type ******//

    /**
     * @return get data types.
     */
    SequenceType getSequenceType();

    /**
     * @return name of data type
     */
    default String getSequenceTypeStr() {
        return Objects.requireNonNull(getSequenceType()).getName();
    }

    //****** States ******//

    /**
     * @return num of states no ambiguous
     */
    default int getCanonicalStateCount() {
        return Objects.requireNonNull(getSequenceType()).getCanonicalStateCount();
    }

    /**
     * @return number of states including ambiguous states
     */
    default int getStateCount() {
        return Objects.requireNonNull(getSequenceType()).getStateCount();
    }

    /**
     * @return a list of {@link State}
     */
    default List<? extends State> getStates() {
        return Objects.requireNonNull(getSequenceType()).getStates();
    }

    /**
     * @return a list of canonical {@link State}
     */
    default List<? extends State> getCanonicalStates() {
        return Objects.requireNonNull(getSequenceType()).getCanonicalStates();
    }

    default String getCharacter(int taxonIndex, int position) {
        int stateInt = getState(taxonIndex, position);
        State state = getSequenceType().getState(stateInt);
        return state.toString();
    }

    /**
     * @param taxonIndex   the taxon index
     * @return   the sequence in a string of a taxon given by index
     */
    default String getSequence(int taxonIndex) {
        StringBuilder builder = new StringBuilder();
        SequenceType sequenceType = getSequenceType();

        State state;
        // loop through sites
        for (int j = 0; j < nchar(); j++) {
            int stateInt = getState(taxonIndex, j);
            // convert int state into letters
            state = sequenceType.getState(stateInt);
            builder.append(Objects.requireNonNull(state));
        }
        return builder.toString();
    }


    //****** Taxa ******//

    default Taxa getTaxa() {
        return this;
    }

    /**
     * @param taxonIndex  the index of a taxon
     * @return            the name of this taxon
     */
    String getTaxonName(int taxonIndex);


    //****** Others ******//

    default Class getComponentType() {
        return Integer.class;
    }

}
