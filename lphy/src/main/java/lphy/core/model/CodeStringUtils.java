package lphy.core.model;

import lphy.core.model.annotation.ParameterInfo;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CodeStringUtils {


    public static String getArgumentCodeString(Map.Entry<String, Value> entry) {
        return getArgumentCodeString(entry.getKey(), entry.getValue());
    }

    public static String getArgumentCodeString(String name, Value value) {
        String prefix = "";
        if (!ExpressionUtils.isInteger(name)) {
            prefix = name + "=";
        }

        if (value == null) {
            throw new RuntimeException("Value of " + name + " is null!");
        }

        if (value.isAnonymous()) return prefix + value.codeString();
        return prefix + value.getId();
    }

    public static String codeString(Func function, Map<String, Value> params) {
        Map<String, Value> map = params;
        Class<?> funcClass = function.getClass();

        StringBuilder builder = new StringBuilder();
        builder.append(function.getName());
        builder.append("(");

        Constructor[] constructors = funcClass.getConstructors();

        if (constructors.length == 1) {
            List<ParameterInfo> parameterInfoList = GeneratorUtils.getParameterInfo(funcClass, 0);
            if (parameterInfoList.size() > 0) {
                int paramCount = 0;

                String name = parameterInfoList.get(0).name();

                if (parameterInfoList.get(0).optional() && map.get(name) == null) {
                    // DO NOTHING - this is an optional parameter with no value
                } else {
                    builder.append(getArgumentCodeString(name, map.get(name)));

                    paramCount += 1;
                }
                for (int i = 1; i < parameterInfoList.size(); i++) {
                    name = parameterInfoList.get(i).name();
                    if (parameterInfoList.get(i).optional() && map.get(name) == null) {
                        // DO NOTHING - this is an optional parameter with no value
                    } else {
                        if (paramCount > 0) builder.append(", ");
                        builder.append(getArgumentCodeString(name, map.get(name)));
                        paramCount += 1;
                    }
                }
            }
        } else {
            Iterator<Map.Entry<String, Value>> iterator = map.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, Value> entry = iterator.next();

                builder.append(getArgumentCodeString(entry));
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    builder.append(", ");
                    builder.append(getArgumentCodeString(entry));
                }
//            }
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
