package lphy.core.model;

import lphy.core.model.annotation.GeneratorInfo;

import java.util.Map;
import java.util.TreeMap;

public abstract class Func implements Generator {

    private String name = null;
    private String description = null;

    public String getName() {
        if (name == null) {
            return(FuncUtils.getName(getClass()));
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

    protected TreeMap<String, Value> paramMap = new TreeMap<>();

    public Map<String, Value> getParams() {
        return paramMap;
    }

    public void setParam(String paramName, Value value) {
        paramMap.put(paramName, value);
    }

    public String codeString() {
        return FuncUtils.codeString(this,getParams());
    }

    @Override
    public char generatorCodeChar() {
        return '=';
    }

}
