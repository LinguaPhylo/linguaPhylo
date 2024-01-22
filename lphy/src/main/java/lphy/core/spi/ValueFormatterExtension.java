package lphy.core.spi;

import lphy.core.logger.ValueFormatter;

import java.util.Map;
import java.util.Set;

/**
 * The service interface defined for SPI.
 * Implement this interface to create one "Container" provider class
 * for each module of LPhy or its extensions,
 * which should include {@link ValueFormatter}.
 *
 * @author Walter Xie
 */
@Deprecated
public interface ValueFormatterExtension extends Extension {


    Set<Class<? extends ValueFormatter>> declareValueFormatters();

//    List<Class<? extends SimulatorListener>> declareSimulatorListeners();

    Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormatters();

}
