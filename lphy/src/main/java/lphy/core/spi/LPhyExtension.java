package lphy.core.spi;

import lphy.core.model.component.Func;
import lphy.core.model.component.GenerativeDistribution;

import java.util.List;

/**
 * The service interface defined for SPI.
 * Implement this interface to create one "Container" provider class
 * for each module of LPhy or its extensions,
 * which should include {@link GenerativeDistribution}, {@link Func}.
 *
 * @author Walter Xie
 */
public interface LPhyExtension {

    /**
     * @return the list of new {@link GenerativeDistribution} implemented in the LPhy extension.
     */
    List<Class<? extends GenerativeDistribution>> getDistributions();

    /**
     * @return the list of new {@link Func} implemented in the LPhy extension.
     */
    List<Class<? extends Func>> getFunctions();

}
