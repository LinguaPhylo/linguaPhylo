package lphy.core.spi;

import java.util.List;

public interface LPhyLoader {

    /**
     * The method to load all classes registered by SPI mechanism.
     */
    void loadAllExtensions();

    /**
     * The method to load classes in a given extension registered by SPI mechanism.
     * @param extClsName The fully qualified class name of the class that implements
     *                   {@link Extension}, such as lphy.spi.LPhyCoreImpl,
     *                   and contains all classes registered for SPI mechanism,
     *                   such as BasicFunction or GenerativeDistribution.
     */
    void loadExtension(String extClsName);

    /**
     * @return a list of {@link Extension} loaded by ServiceLoader for extension manager or doc.
     */
    List<? extends Extension> getExtensions();
}
