package lphy.core.spi;

import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The interface to define the registration of
 * {@link GenerativeDistribution}, {@link BasicFunction}.
 *
 * @author Walter Xie
 */
public interface LPhyExtension extends Extension {

    /**
     * @return the list of new {@link GenerativeDistribution} implemented in the LPhy extension.
     */
    List<Class<? extends GenerativeDistribution>> declareDistributions();

    /**
     * @return the list of new {@link BasicFunction} implemented in the LPhy extension.
     */
    List<Class<? extends BasicFunction>> declareFunctions();


    Map<String, Set<Class<?>>> getDistributions();

    Map<String, Set<Class<?>>> getFunctions();

    TreeSet<Class<?>> getTypes();

}
