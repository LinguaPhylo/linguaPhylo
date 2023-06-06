package lphy.core.model;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.parser.argument.ArgumentUtils;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FuncUtils {
    public static String getName(Class<? extends Func> funcClass) {
        GeneratorInfo fInfo = GeneratorUtils.getGeneratorInfo(funcClass);
        if (fInfo != null) {
            return fInfo.name();
        } else return funcClass.getSimpleName();
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
                    builder.append(ArgumentUtils.getArgumentCodeString(name, map.get(name)));

                    paramCount += 1;
                }
                for (int i = 1; i < parameterInfoList.size(); i++) {
                    name = parameterInfoList.get(i).name();
                    if (parameterInfoList.get(i).optional() && map.get(name) == null) {
                        // DO NOTHING - this is an optional parameter with no value
                    } else {
                        if (paramCount > 0) builder.append(", ");
                        builder.append(ArgumentUtils.getArgumentCodeString(name, map.get(name)));
                        paramCount += 1;
                    }
                }
            }
        } else {
            Iterator<Map.Entry<String, Value>> iterator = map.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, Value> entry = iterator.next();

                builder.append(ArgumentUtils.getArgumentCodeString(entry));
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    builder.append(", ");
                    builder.append(ArgumentUtils.getArgumentCodeString(entry));
                }
//            }
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
