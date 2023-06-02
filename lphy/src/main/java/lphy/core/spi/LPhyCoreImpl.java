package lphy.core.spi;

import lphy.core.model.components.Func;
import lphy.core.model.components.GenerativeDistribution;
import lphy.core.vectorization.operation.Range;
import lphy.core.vectorization.operation.SliceDoubleArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link GenerativeDistribution}, {@link Func} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyCoreImpl implements LPhyExtension {

    List<Class<? extends Func>> functions = Arrays.asList(
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
    public List<Class<? extends Func>> getFunctions() {
        return functions;
    }

}
