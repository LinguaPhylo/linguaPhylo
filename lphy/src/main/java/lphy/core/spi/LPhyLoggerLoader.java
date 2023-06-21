package lphy.core.spi;

import lphy.core.logger.RandomValueLogger;

import java.util.*;

/**
 * The implementation to load LPhy extensions using {@link ServiceLoader}.
 * All distributions, functions and data types will be collected
 * in this class for later use.
 *
 * @author Walter Xie
 */
public class LPhyLoggerLoader {
    private ServiceLoader<LPhySimLogger> loader;

    // Required by ServiceLoader
    public LPhyLoggerLoader() { }

    /**
     * Simulation result loggers
     */
    public Map<String, Set<Class<?>>> simulationLoggers;

    /**
     * The method to load all classes registered by SPI mechanism.
     */
    public void loadAllExtensions() {
        if (loader == null)
            loader = ServiceLoader.load(LPhySimLogger.class);

        registerExtensions(null);
    }

    // if extClsName is null, then load all classes,
    // otherwise load classes in a given extension.
    private void registerExtensions(String extClsName) {

        simulationLoggers = new TreeMap<>();

        try {
            //*** LPhyCoreSimLoggerImpl must have a public no-args constructor ***//
            for (LPhySimLogger lPhyLoggerExt : loader) {
                // loggers
                List<Class<? extends RandomValueLogger>> loggers = lPhyLoggerExt.getSimulationLoggers();

                for (Class<? extends RandomValueLogger> loggerClass : loggers) {
                    String name = loggerClass.getSimpleName();

                    Set<Class<?>> loggerSet = simulationLoggers.computeIfAbsent(name, k -> new HashSet<>());
                    loggerSet.add(loggerClass);
                }
            }
            System.out.println("LPhy simulation loggers : " + Arrays.toString(simulationLoggers.keySet().toArray()));

        } catch (ServiceConfigurationError serviceError) {
            System.err.println(serviceError);
            serviceError.printStackTrace();
        }

    }


}
