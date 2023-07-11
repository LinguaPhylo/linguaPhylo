package lphy.base.spi;

import lphy.base.logger.NexusAlignmentFormatter;
import lphy.base.logger.NexusTreeFormatter;
import lphy.core.logger.ValueFormatter;
import lphy.core.spi.LPhyValueFormatter;

import java.util.Set;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link ValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyBaseValueFormatterImpl implements LPhyValueFormatter {

    /**
     * Required by ServiceLoader.
     */
    public LPhyBaseValueFormatterImpl() {
    }

    @Override
    public Set<Class<? extends ValueFormatter>> getValueFormatters() {
        return Set.of(NexusAlignmentFormatter.class, NexusTreeFormatter.class);
    }
//    public Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormatterMap() {
//        return Map.of( SimpleAlignment.class, Set.of(NexusAlignmentFormatter.class),
//                TimeTree.class, Set.of(NexusTreeFormatter.class) );
//    }


    public String getExtensionName() {
        return "LPhy base loggers";
    }
}
