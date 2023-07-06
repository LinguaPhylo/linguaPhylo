package lphystudio.spi;

import lphy.core.logger.RandomValueFormatter;
import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.spi.LPhyExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * Empty class to show studio ext in the Extension Manager.
 * @author Walter Xie
 */
public class LPhyStudioImpl implements LPhyExtension {

    /**
     * Required by ServiceLoader.
     */
    public LPhyStudioImpl() {
    }

    @Override
    public List<Class<? extends GenerativeDistribution>> getDistributions() {
        return new ArrayList<>();
    }

    @Override
    public List<Class<? extends BasicFunction>> getFunctions() {
        return new ArrayList<>();
    }

    public List<Class<? extends RandomValueFormatter>> getSimulationLoggers() {
        return new ArrayList<>();
    }

    public String getExtensionName() {
        return "LPhy studio";
    }
}
