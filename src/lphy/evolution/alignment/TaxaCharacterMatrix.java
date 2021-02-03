package lphy.evolution.alignment;

import lphy.evolution.HasTaxa;
import lphy.evolution.NChar;
import lphy.evolution.TaxaData;
import lphy.graphicalModel.MultiDimensional;

import java.lang.reflect.Array;

public interface TaxaCharacterMatrix<T> extends NChar, HasTaxa, MultiDimensional {

    /**
     * @param taxon
     * @param characterColumn
     * @return the character for the given taxon
     */
    T getState(String taxon, int characterColumn);

    /**
     * @return the class of this character (e.g. Double for continuous characters, Integer for discrete characters)
     */
    Class getComponentType();


    /**
     * @param taxon the taxon to get a sequence of
     * @return an array of character states for the given taxon
     */
    default T[] getCharacterSequence(String taxon) {
        T[] sequence = (T[]) Array.newInstance(getComponentType(),nchar());
        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = getState(taxon, i);
        }
        return sequence;
    }

    /**
     * @return  a JSON for pretty printing
     */
    String toJSON();
}
