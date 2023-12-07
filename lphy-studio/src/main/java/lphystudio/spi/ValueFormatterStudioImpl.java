package lphystudio.spi;

import lphy.core.logger.ValueFormatter;
import lphy.core.spi.ValueFormatterCoreImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO: it is unused now, overwrite the register() when any new classes being to add.
 * The "Container" provider class that implements SPI
 * which include a list of {@link ValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class ValueFormatterStudioImpl extends ValueFormatterCoreImpl {//implements ValueFormatterExtension {
//    List<Class<? extends ValueFormatter>> valueFormatters = Arrays.asList(
//            AlignmentLog.class, TreeLog.class, VariableLog.class, VariableSummaryLog.class);
    @Override
    public Set<Class<? extends ValueFormatter>> declareValueFormatters() {
        return new HashSet<>();
    }

    /**
     * Required by ServiceLoader.
     */
    public ValueFormatterStudioImpl() {
    }

    public String getExtensionName() {
        return "LPhy studio loggers";
    }
}
