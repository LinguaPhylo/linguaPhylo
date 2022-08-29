package lphy.evolution;

import lphy.graphicalModel.GeneratorCategory;
import lphy.graphicalModel.MethodInfo;
import lphy.graphicalModel.TypeInfo;

/**
 * An interface that site-dimensioned objects can implement, such as Alignment.
 */
@TypeInfo(description = "An interface that site-dimensioned objects can implement, such as Alignment.",
        examples = {"simpleSerialCoalescentNex.lphy"})
public interface NChar {

    /**
     * @return the number of sites this object has.
     */
    @MethodInfo(description="The number of characters/sites.", narrativeName = "number of characters",
            category = GeneratorCategory.TAXA_ALIGNMENT, examples = {"simpleSerialCoalescentNex.lphy"})
    Integer nchar();
}
