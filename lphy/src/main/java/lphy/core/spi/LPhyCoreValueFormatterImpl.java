package lphy.core.spi;

import lphy.core.logger.ValueFormatter;

import java.util.HashSet;
import java.util.Set;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link ValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyCoreValueFormatterImpl implements LPhyValueFormatter {

    /**
     * Required by ServiceLoader.
     */
    public LPhyCoreValueFormatterImpl() { }

    @Override
    public Set<Class<? extends ValueFormatter>> getValueFormatters() {
//        return Set.of(ValueFormatter.Base.class);
        return new HashSet<>();
    }


    public String getExtensionName() {
        return "LPhy core loggers";
    }
}
