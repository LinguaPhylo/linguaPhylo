package lphy.base.spi;

import lphy.base.logger.AlignmentFileLogger;
import lphy.base.logger.TreeFileLogger;
import lphy.core.logger.RandomValueLogger;
import lphy.core.spi.LPhySimLogger;

import java.util.Arrays;
import java.util.List;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link RandomValueLogger} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyBaseSimLoggerImpl implements LPhySimLogger {
    List<Class<? extends RandomValueLogger>> simulationLoggers = Arrays.asList(
            AlignmentFileLogger.class, TreeFileLogger.class);

    /**
     * Required by ServiceLoader.
     */
    public LPhyBaseSimLoggerImpl() {
        //TODO do something here, e.g. print package or classes info ?
    }
    @Override
    public List<Class<? extends RandomValueLogger>> getSimulationLoggers() {
        return simulationLoggers;
    }

    public String getExtensionName() {
        return "LPhy base loggers";
    }
}
