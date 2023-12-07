package lphystudio.spi;

import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.spi.LPhyCoreImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: it is unused now, overwrite the register() when any new classes being to add.
 * Empty class to show studio ext in the Extension Manager.
 * @author Walter Xie
 */
public class LPhyStudioImpl extends LPhyCoreImpl { //} implements LPhyExtension {

    @Override
    public List<Class<? extends GenerativeDistribution>> declareDistributions() {
        return new ArrayList<>();
    }

    @Override
    public List<Class<? extends BasicFunction>> declareFunctions() {
        return new ArrayList<>();
    }

    /**
     * Required by ServiceLoader.
     */
    public LPhyStudioImpl() {
    }


    public String getExtensionName() {
        return "LPhy studio";
    }
}
