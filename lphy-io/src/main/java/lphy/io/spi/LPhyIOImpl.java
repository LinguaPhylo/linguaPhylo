package lphy.io.spi;

import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.spi.LPhyExtension;
import lphy.io.function.Simulate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link GenerativeDistribution}, {@link BasicFunction} to extend.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyIOImpl implements LPhyExtension {

    List<Class<? extends BasicFunction>> functions = Arrays.asList(Simulate.class);

    /**
     * Required by ServiceLoader.
     */
    public LPhyIOImpl() {
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
    public String getExtensionName() {
        return "LPhy IO library";
    }

}
