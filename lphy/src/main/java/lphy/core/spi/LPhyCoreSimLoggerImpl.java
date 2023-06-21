package lphy.core.spi;

import lphy.core.logger.RandomValueLogger;
import lphy.core.logger.VarFileLogger;

import java.util.Arrays;
import java.util.List;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link RandomValueLogger} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyCoreSimLoggerImpl implements LPhySimLogger {
    List<Class<? extends RandomValueLogger>> simulationLoggers = Arrays.asList(
            VarFileLogger.class);

    /**
     * Required by ServiceLoader.
     */
    public LPhyCoreSimLoggerImpl() {
        //TODO do something here, e.g. print package or classes info ?
    }
    @Override
    public List<Class<? extends RandomValueLogger>> getSimulationLoggers() {
        return simulationLoggers;
    }

    public String getExtensionName() {
        return "LPhy core loggers";
    }
}
