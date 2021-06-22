package lphy.evolution;

import lphy.graphicalModel.MethodInfo;

/**
 * An interface that site-dimensioned objects can implement, such as Alignment.
 */
public interface NChar {

    /**
     * @return the number of sites this object has.
     */
    @MethodInfo(description="The number of characters/sites.", narrativeName = "number of characters")
    Integer nchar();
}
