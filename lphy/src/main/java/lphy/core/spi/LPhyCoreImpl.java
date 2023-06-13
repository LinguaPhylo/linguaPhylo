package lphy.core.spi;

import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.vectorization.operation.Range;
import lphy.core.vectorization.operation.SliceDoubleArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link GenerativeDistribution}, {@link BasicFunction} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyCoreImpl implements LPhyExtension {

    List<Class<? extends BasicFunction>> functions = Arrays.asList(
            Range.class, SliceDoubleArray.class);

    /**
     * Required by ServiceLoader.
     */
    public LPhyCoreImpl() {
        //TODO do something here, e.g. print package or classes info ?
    }

    @Override
    public List<Class<? extends GenerativeDistribution>> getDistributions() {
        return new ArrayList<>();
    }

    @Override
    public List<Class<? extends BasicFunction>> getFunctions() {
        return functions;
    }

    @Override
    public String getName() {
        return "LPhy core";
    }
}
