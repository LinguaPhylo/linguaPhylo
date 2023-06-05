package lphy.core.model.component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class ReflectUtils {

    public static Class getGenericReturnType(Method method) {
        return getClass(method.getGenericReturnType());
    }

    /**
     * @param type the type signature for a return value or parameter
     * @return the generic class. e.g. if type is {@code lphy.graphicalModel.Value<java.lang.Number>} then this will return java.lang.Number.class
     */
    public static Class getClass(Type type) {
        Class typeClass;
        String name = type.getTypeName();

        if (name.indexOf('<') >= 0) {

            String typeClassString = name.substring(name.indexOf('<') + 1, name.lastIndexOf('>'));

            if (typeClassString.endsWith("[]")) {
                typeClassString = "L" + typeClassString;

                while (typeClassString.endsWith("[]")) {
                    typeClassString = "[" + typeClassString.substring(0, typeClassString.lastIndexOf('['));
                }
                typeClassString = typeClassString + ";";
            }

            try {
                typeClass = Class.forName(typeClassString);
            } catch (ClassNotFoundException e) {
                // TODO need to understand these cases better!
                typeClass = Object.class;
            }
        } else typeClass = Object.class;
        return typeClass;
    }
}
