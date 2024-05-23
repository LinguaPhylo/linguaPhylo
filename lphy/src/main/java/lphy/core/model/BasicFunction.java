package lphy.core.model;

import lphy.core.model.annotation.GeneratorInfo;

import java.util.Map;
import java.util.TreeMap;

/**
 * TODO why we need this ? Merge with DeterministicFunction
 */
public abstract class BasicFunction implements Generator {

    private String name = null;
    private String description = null;

    public static String getName(Class<? extends BasicFunction> funcClass) {
        GeneratorInfo fInfo = GeneratorUtils.getGeneratorInfo(funcClass);
        if (fInfo != null) {
            return fInfo.name();
        } else return funcClass.getSimpleName();
    }

    public String getName() {
        if (name == null) {
            return(getName(getClass()));
        }
        return name;
    }

    public String getDescription() {
        if (description == null) {
            GeneratorInfo fInfo = GeneratorUtils.getGeneratorInfo(getClass());
            if (fInfo != null) {
                description = fInfo.name();
            } else description = getClass().getSimpleName();
        }
        return description;
    }

    protected Map<String, Value> paramMap = new TreeMap<>();

    public Map<String, Value> getParams() {
        return paramMap;
    }

    public void setParam(String paramName, Value value) {
        paramMap.put(paramName, value);
    }

    public String codeString() {
        return CodeStringUtils.codeString(this,getParams());
    }

    @Override
    public char generatorCodeChar() {
        return '=';
    }

}
