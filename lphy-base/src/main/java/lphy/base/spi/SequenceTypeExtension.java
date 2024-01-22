package lphy.base.spi;

import jebl.evolution.sequences.SequenceType;
import lphy.core.spi.Extension;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * The service interface defined for SPI.
 * Implement this interface to create one "Container" provider class
 * for each module of LPhy or its extensions,
 * which should include {@link SequenceType}.
 *
 * @author Walter Xie
 */
public interface SequenceTypeExtension extends Extension {

    /**
     * @return the map of new {@link SequenceType} implemented in the LPhy extension.
     *         The string key is a keyword to represent this SequenceType.
     *         The keyword can be used to identify and initialise the corresponding sequence type.
     */
    Map<String, ? extends SequenceType> declareSequenceTypes();

    Set<SequenceType> getSequenceTypes();

    /**
     * Add new types from {@link #declareSequenceTypes()} into sequenceTypeMap.
     * @param newTypes         new types defined by {@link #declareSequenceTypes()}.
     * @param sequenceTypeMap  the map to store all SequenceType for this extension.
     * @param message          information message.
     */
    default void addSequenceTypes(Map<String, ? extends SequenceType> newTypes,
                                  Map<String, SequenceType> sequenceTypeMap,
                                  String message) {
        if (newTypes != null)
            // TODO validate same sequence type?
            newTypes.forEach(sequenceTypeMap::putIfAbsent);

        System.out.println(message + Arrays.toString(getSequenceTypes().toArray(new SequenceType[0])));
    }

}
