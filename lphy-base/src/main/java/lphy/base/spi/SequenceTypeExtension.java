package lphy.base.spi;

import jebl.evolution.sequences.SequenceType;

import java.util.Map;

/**
 * The service interface defined for SPI.
 * Implement this interface to create one "Container" provider class
 * for each module of LPhy or its extensions,
 * which should include {@link SequenceType}.
 *
 * @author Walter Xie
 */
public interface SequenceTypeExtension {

    /**
     * @return the map of new {@link SequenceType} implemented in the LPhy extension.
     *         The string key is a keyword to represent this SequenceType.
     *         The keyword can be used to identify and initialise the corresponding sequence type.
     */
    Map<String, ? extends SequenceType> getSequenceTypes();

}
