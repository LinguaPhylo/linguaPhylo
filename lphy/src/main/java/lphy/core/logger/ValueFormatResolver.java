package lphy.core.logger;

import lphy.core.model.Value;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Resolve the multiple ValueFormatter mapping to the same data type.
 * Pick up the 1st as default.
 */
public class ValueFormatResolver {

    // after newInstance
    private static Map<Class<?>, ValueFormatter> resolvedFormatters = new HashMap<>();
    private static final int DEFAULT_FORMATTER = 0; // 1st formatter

    public ValueFormatResolver(Map<Class<?>, Set<Class<? extends ValueFormatter>>> valueFormatterClasses) {
        resolveFormatters(valueFormatterClasses);
    }

    //TODO add strategies

    private void resolveFormatters(Map<Class<?>, Set<Class<? extends ValueFormatter>>> valueFormatterClasses) {

        for (Map.Entry<Class<?>, Set<Class<? extends ValueFormatter>>> entry : valueFormatterClasses.entrySet()) {
            Class<?> cls = entry.getKey();
            Set<Class<? extends ValueFormatter>> fmClsSet = entry.getValue();

            // TODO
            ValueFormatter f = null;
            try {
                f = resolveFormatter(cls, fmClsSet);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | IllegalArgumentException e) {
                throw new RuntimeException(e);
            }

            resolvedFormatters.put(cls, f);
        }
    }


    private ValueFormatter resolveFormatter(Class<?> cls, Set<Class<? extends ValueFormatter>> formatterClasses)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException, IllegalArgumentException{

        if (formatterClasses.size() < 1)
            //TODO inherited data type, such as a different Alignment
//            return new ValueFormatter<T>() { };
          throw new IllegalArgumentException("Cannot find the formatter for " + cls.getName());

        List<Class<? extends ValueFormatter>> vFClsist = new ArrayList<>(formatterClasses);

        //TODO more strategies here

        // default to take the 1st
        Class<? extends ValueFormatter> vFCls = vFClsist.get(DEFAULT_FORMATTER);

        // create instance
        return vFCls.getDeclaredConstructor().newInstance();
    }


    public ValueFormatter getFormatter(Value value) {
        Class cls = value.getType();
        return resolvedFormatters.get(cls);
    }
}
