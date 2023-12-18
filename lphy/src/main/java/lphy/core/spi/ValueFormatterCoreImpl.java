package lphy.core.spi;

import lphy.core.logger.LoggerUtils;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.GeneratorUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link ValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class ValueFormatterCoreImpl implements ValueFormatterExtension {

    @Override
    public Set<Class<? extends ValueFormatter>> declareValueFormatters() {
//        return Set.of(ValueFormatter.Base.class);
        return new HashSet<>();
    }

    /**
     * Required by ServiceLoader.
     */
    public ValueFormatterCoreImpl() { }

    /**
     * Key is data type, e.g. Integer.class, value is the Set of ValueFormatter assigned to this type.
     */
    protected Map<Class<?>, Set<Class<? extends ValueFormatter>>> valueFormatterClasses;


    @Override
    public void register() {
        valueFormatterClasses = new HashMap<>();

        // ValueFormatter
        Set<Class<? extends ValueFormatter>> formatterSet = declareValueFormatters();

        //TODO better code ?
//  for (Map.Entry<Class<?>, Set<Class<? extends ValueFormatter>>> entry : formatterMap.entrySet()) {
        for (Class<? extends ValueFormatter> vFCls : formatterSet) {
            // get the data type
            // Class<?> typeCls = vFCls.getTypeParameters()[0].getClass();
            Class<?> typeCls = null;
            try {
                Method method = vFCls.getMethod("getDataTypeClass");
                typeCls = GeneratorUtils.getGenericReturnType(method);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (typeCls == null)
                LoggerUtils.log.severe("Extension " + getExtensionName() +
                        " : ValueFormatter '" + vFCls.getName() + "' cannot find data type !");

            Set<Class<? extends ValueFormatter>> valFormSet = valueFormatterClasses
                    .computeIfAbsent(typeCls, k -> new HashSet<>());
//                        Set<Class<? extends ValueFormatter>> newValFormSet = entry.getValue();

            // warning, if the same ValueFormatter exists for the same Class in valueFormatterClasses map
//                        newValFormSet.forEach(dataType -> {
            if (valFormSet.contains(vFCls))
                LoggerUtils.log.warning("Extension " + getExtensionName() +
                        " : ValueFormatter '" + vFCls.getName() + "' already exists in ValueFormatter map !");
            valFormSet.add(vFCls);
//                        });

        }
        // SimulatorListener
//                List<Class<? extends SimulatorListener>> listeners = valueFormatterSPI.getSimulatorListenerClasses();
//                LoaderManager.registerClasses(listeners, simulatorListeners);

    }

    public Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormatters() {
        return valueFormatterClasses;
    }

    public String getExtensionName() {
        return "LPhy core loggers";
    }
}
