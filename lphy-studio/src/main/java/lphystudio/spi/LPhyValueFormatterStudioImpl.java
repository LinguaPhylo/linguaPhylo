package lphystudio.spi;

import lphy.core.logger.ValueFormatter;
import lphy.core.spi.LPhyValueFormatter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link ValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyValueFormatterStudioImpl implements LPhyValueFormatter {
//    List<Class<? extends ValueFormatter>> valueFormatters = Arrays.asList(
//            AlignmentLog.class, TreeLog.class, VariableLog.class, VariableSummaryLog.class);
    /**
     * Required by ServiceLoader.
     */
    public LPhyValueFormatterStudioImpl() {
    }
    @Override
    public Set<Class<? extends ValueFormatter>> declareValueFormatters() {
        return new HashSet<>();
    }

    @Override
    public Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormatters() {
        return null;
    }
//    public Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormatterMap() {
//        return new HashMap<>();
//    }

    @Override
    public void register() {

    }

    public String getExtensionName() {
        return "LPhy studio loggers";
    }
}
