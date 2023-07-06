package lphystudio.spi;

import lphy.core.logger.RandomValueFormatter;
import lphy.core.logger.ValueFormatter;
import lphy.core.simulator.SimulatorListener;
import lphy.core.spi.LPhyValueFormatter;

import java.util.*;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link RandomValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyStudioLoggerImpl implements LPhyValueFormatter {
//    List<Class<? extends ValueFormatter>> valueFormatters = Arrays.asList(
//            AlignmentLog.class, TreeLog.class, VariableLog.class, VariableSummary.class);
    /**
     * Required by ServiceLoader.
     */
    public LPhyStudioLoggerImpl() {
    }
    @Override
    public Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormatterMap() {
        return new HashMap<>();
    }

    @Override
    public List<Class<? extends SimulatorListener>> getSimulatorListenerClasses() {
        return new ArrayList<>();
    }

    public String getExtensionName() {
        return "LPhy studio loggers";
    }
}
