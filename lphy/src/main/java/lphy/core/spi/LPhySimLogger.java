package lphy.core.spi;

import lphy.core.logger.RandomValueLogger;

import java.util.List;

/**
 * The service interface defined for SPI.
 * Implement this interface to create one "Container" provider class
 * for each module of LPhy or its extensions,
 * which should include {@link RandomValueLogger}.
 *
 * @author Walter Xie
 */
public interface LPhySimLogger {


    List<Class<? extends RandomValueLogger>> getSimulationLoggers();

}
