package lphy.core.spi;

/**
 * The service interface defined for SPI.
 * Implement this interface to create one "Container" provider class
 * for each module of LPhy or its extensions.
 * @author Walter Xie
 */
public interface Extension {

    /**
     * The main method to register all classes and save them into a Map or Set,
     * which will be used by {@link LoaderManager} to create their instances.
     * The process will be different and related to the roles of these extensions doing,
     * such as registering BasicFunction or GenerativeDistribution, and resolving ValueFormatter, etc.
     */
    void register();


    default String getModuleName() {
        Module module = getClass().getModule();
        return module.getName();
    }

    default String getExtensionName() {
        return getModuleName() + "." + getClass().getSimpleName();
    }

}
