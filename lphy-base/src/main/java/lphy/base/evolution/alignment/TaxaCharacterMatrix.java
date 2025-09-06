package lphy.base.evolution.alignment;

import lphy.base.evolution.HasTaxa;
import lphy.base.evolution.NChar;
import lphy.base.evolution.Taxa;
import lphy.core.model.MultiDimensional;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.MethodInfo;

import java.lang.reflect.Array;

public interface TaxaCharacterMatrix<T> extends NChar, HasTaxa, MultiDimensional {

    /**
     * @param taxon
     * @param characterColumn
     * @return the character for the given taxon
     */
    T getState(String taxon, int characterColumn);

    /**
     * Set int states.
     * @param taxon      the index of taxon in the 1st dimension.
     * @param position   the site position in the 2nd dimension.
     * @param state      the state in integer
     */
    void setState(int taxon, int position, T state);

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
