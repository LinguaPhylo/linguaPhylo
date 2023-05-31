package lphy.base.evolution;

import lphy.core.graphicalmodel.components.GeneratorCategory;
import lphy.core.graphicalmodel.components.MethodInfo;
import lphy.core.graphicalmodel.components.TypeInfo;

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
