package lphy.base.spi;

import lphy.base.logger.VCFFormatter;
import lphy.base.logger.NexusAlignmentFormatter;
import lphy.base.logger.NexusTreeFormatter;
import lphy.core.logger.ValueFormatter;
import lphy.core.spi.ValueFormatterCoreImpl;

import java.util.Set;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link ValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
@Deprecated
public class ValueFormatterBaseImpl extends ValueFormatterCoreImpl {//implements ValueFormatterExtension {

    //TODO NexusAlignmentFormatter.class, NexusTreeFormatter.class will be used inside lphy only.
    // extension mechanism is implemented in TextFileFormatted now.
    @Override
    public Set<Class<? extends ValueFormatter>> declareValueFormatters() {
        return Set.of(NexusAlignmentFormatter.class, NexusTreeFormatter.class,
                VCFFormatter.class);
    }

    /**
     * Required by ServiceLoader.
     */
    public ValueFormatterBaseImpl() {
    }

    public String getExtensionName() {
        return "LPhy base loggers";
    }
}
