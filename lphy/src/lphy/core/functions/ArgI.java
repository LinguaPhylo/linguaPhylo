package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.util.SortedMap;
import java.util.TreeMap;

public class ArgI extends DeterministicFunction<Integer> {

    private final String nameParamName;
    private final String defaultParamName;
    private Value<String> name;
    private Value<Integer> defaultValue;

    private static java.util.Map<String, Integer> integerArguments = new TreeMap<>();


    public ArgI(@ParameterInfo(name = "name", description = "the name of the integer argument.") Value<String> name,
                @ParameterInfo(name = "default", description = "the default value.", optional = true) Value<Integer> defaultValue) {

        this.name = name;
        if (name == null) throw new IllegalArgumentException("The mean value can't be null!");
        this.defaultValue = defaultValue;

        nameParamName = getParamName(0);
        defaultParamName = getParamName(1);
    }

    @GeneratorInfo(name = "argi", description = "The arg function for reading an integer.")
    public Value<Integer> apply() {
        Integer value = integerArguments.get(name.value());

        if (value != null) {
            return new Value<>(value, this);
        }

        return defaultValue;
    }

    public java.util.Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(nameParamName, name);
        map.put(defaultParamName, defaultValue);
        return map;
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(nameParamName)) name = value;
        else if (paramName.equals(defaultParamName)) defaultValue = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public static void putArgument(String name, int value) {
        integerArguments.put(name, value);
    }
}