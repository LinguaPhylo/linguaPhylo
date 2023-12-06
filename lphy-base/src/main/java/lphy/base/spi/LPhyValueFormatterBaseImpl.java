package lphy.base.spi;

import lphy.base.logger.NexusAlignmentFormatter;
import lphy.base.logger.NexusTreeFormatter;
import lphy.core.logger.ValueFormatter;
import lphy.core.spi.LPhyValueFormatterCoreImpl;

import java.util.Set;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link ValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyValueFormatterBaseImpl extends LPhyValueFormatterCoreImpl {//implements LPhyValueFormatter {


    @Override
    public Set<Class<? extends ValueFormatter>> declareValueFormatters() {
        return Set.of(NexusAlignmentFormatter.class, NexusTreeFormatter.class);
    }

    /**
     * Required by ServiceLoader.
     */
    public LPhyValueFormatterBaseImpl() {
    }

    public String getExtensionName() {
        return "LPhy base loggers";
    }
}
