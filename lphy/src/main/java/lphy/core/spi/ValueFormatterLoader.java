package lphy.core.spi;

import lphy.core.logger.FormatterRegistry;
import lphy.core.logger.LoggerUtils;
import lphy.core.logger.ValueFormatter;

import java.util.*;

/**
 * The implementation to load LPhy extensions using {@link ServiceLoader}.
 * All distributions, functions and data types will be collected
 * in this class for later use.
 *
 * @author Walter Xie
 */
public class ValueFormatterLoader {
    private ServiceLoader<LPhyValueFormatter> loader;

    // Required by ServiceLoader
    public ValueFormatterLoader() { }

    /**
     * Key is data type, e.g. Integer.class, value is the Set of ValueFormatter assigned to this type.
     */
    private Map<Class<?>, Set<Class<? extends ValueFormatter>>> valueFormatterClasses;

    private Map<String, Set<Class<?>>> simulatorListeners;

    /**
     * The method to load all classes registered by SPI mechanism.
     */
    public void loadAllExtensions() {
        if (loader == null)
            loader = ServiceLoader.load(LPhyValueFormatter.class);

        registerExtensions(null);
    }

    // if extClsName is null, then load all classes,
    // otherwise load classes in a given extension.
    private void registerExtensions(String extClsName) {

        valueFormatterClasses = new HashMap<>();
        // init with core data types
        valueFormatterClasses.putAll(FormatterRegistry.getAllFormattersMap());

//        simulatorListeners = new HashMap<>();

        try {
            for (LPhyValueFormatter valueFormatterSPI : loader) {

                if (extClsName == null || valueFormatterSPI.getClass().getName().equalsIgnoreCase(extClsName)) {
                    System.out.println("Registering extension from " + valueFormatterSPI.getClass().getName());
                    // ValueFormatter
                    Map<Class<?>, Set<Class<? extends ValueFormatter>>> formatterMap = valueFormatterSPI.getValueFormatterMap();

                    //TODO better code ?
                    for (Map.Entry<Class<?>, Set<Class<? extends ValueFormatter>>> entry : formatterMap.entrySet()) {
                        Class<?> cls = entry.getKey();
                        Set<Class<? extends ValueFormatter>> valFormSet = valueFormatterClasses.computeIfAbsent(cls, k -> new HashSet<>());
                        Set<Class<? extends ValueFormatter>> newValFormSet = entry.getValue();

                        // warning, if the same ValueFormatter exists for the same Class in valueFormatterClasses map
                        newValFormSet.forEach(dataType -> {
                            if (valFormSet.contains(dataType))
                                LoggerUtils.log.warning("Extension " + valueFormatterSPI.getExtensionName() +
                                        " : data type '" + dataType + "' already exists in ValueFormatter map !");
                            valFormSet.add(dataType);
                        });

                    }
                    // SimulatorListener
//                List<Class<? extends SimulatorListener>> listeners = valueFormatterSPI.getSimulatorListenerClasses();
//                LoaderManager.registerClasses(listeners, simulatorListeners);
                }
            }

        System.out.println("Register value formatter for : " +
                Arrays.toString(valueFormatterClasses.keySet().stream()
                        // sorted SimpleName of Classes
                        .map(Class::getSimpleName).sorted().toArray() ));

//        System.out.println("LPhy simulator listener : " + Arrays.toString(simulatorListeners.keySet().toArray()));


        } catch (ServiceConfigurationError serviceError) {
            System.err.println(serviceError);
            serviceError.printStackTrace();
        }

    }

    public Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormattersClasses() {
        return valueFormatterClasses;
    }

//    public Map<String, Set<Class<?>>> getSimulatorListeners() {
//        return simulatorListeners;
//    }
}
