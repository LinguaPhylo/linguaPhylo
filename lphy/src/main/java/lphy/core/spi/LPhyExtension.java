package lphy.core.spi;

import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;

import java.util.List;

/**
 * The service interface defined for SPI.
 * Implement this interface to create one "Container" provider class
 * for each module of LPhy or its extensions,
 * which should include {@link GenerativeDistribution}, {@link BasicFunction}.
 *
 * @author Walter Xie
 */
public interface LPhyExtension extends Extension {

    /**
     * @return the list of new {@link GenerativeDistribution} implemented in the LPhy extension.
     */
    List<Class<? extends GenerativeDistribution>> getDistributions();

    /**
     * @return the list of new {@link BasicFunction} implemented in the LPhy extension.
     */
    List<Class<? extends BasicFunction>> getFunctions();

}
