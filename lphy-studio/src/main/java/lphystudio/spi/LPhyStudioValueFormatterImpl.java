package lphystudio.spi;

import lphy.core.logger.RandomValueFormatter;
import lphy.core.logger.ValueFormatter;
import lphy.core.spi.LPhyValueFormatter;

import java.util.HashSet;
import java.util.Set;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link RandomValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyStudioValueFormatterImpl implements LPhyValueFormatter {
//    List<Class<? extends ValueFormatter>> valueFormatters = Arrays.asList(
//            AlignmentLog.class, TreeLog.class, VariableLog.class, VariableSummary.class);
    /**
     * Required by ServiceLoader.
     */
    public LPhyStudioValueFormatterImpl() {
    }
    @Override
    public Set<Class<? extends ValueFormatter>> getValueFormatters() {
        return new HashSet<>();
    }
//    public Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormatterMap() {
//        return new HashMap<>();
//    }

    public String getExtensionName() {
        return "LPhy studio loggers";
    }
}
