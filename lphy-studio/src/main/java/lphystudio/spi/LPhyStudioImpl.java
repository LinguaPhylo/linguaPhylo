package lphystudio.spi;

import lphy.core.spi.LPhyCoreImpl;

/**
 * It is empty, but used to show studio ext in the LPhyExtension Manager.
 * @author Walter Xie
 */
public class LPhyStudioImpl extends LPhyCoreImpl {


    /**
     * Required by ServiceLoader.
     */
    public LPhyStudioImpl() {
    }


    public String getExtensionName() {
        return "LPhy studio";
    }
}
