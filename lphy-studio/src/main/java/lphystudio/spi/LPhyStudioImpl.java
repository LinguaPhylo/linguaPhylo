package lphystudio.spi;

import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.spi.LPhyCoreImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Empty class to show studio ext in the Extension Manager.
 * @author Walter Xie
 */
public class LPhyStudioImpl extends LPhyCoreImpl { //} implements LPhyExtension {

    /**
     * Required by ServiceLoader.
     */
    public LPhyStudioImpl() {
    }

    @Override
    public List<Class<? extends GenerativeDistribution>> declareDistributions() {
        return new ArrayList<>();
    }

    @Override
    public List<Class<? extends BasicFunction>> declareFunctions() {
        return new ArrayList<>();
    }



    public String getExtensionName() {
        return "LPhy studio";
    }
}
