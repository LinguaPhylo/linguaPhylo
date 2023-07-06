package lphy.core.spi;

import lphy.core.logger.ValueFormatter;
import lphy.core.simulator.SimulatorListener;

import java.util.List;
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
public interface LPhyValueFormatter extends Extension {


    Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormatterMap();

    List<Class<? extends SimulatorListener>> getSimulatorListenerClasses();



}
