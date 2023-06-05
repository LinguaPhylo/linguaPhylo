package lphy.base.function;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.Value;

import java.util.TreeMap;

public class ArgI extends DeterministicFunction<Integer> {

    private static final String nameParamName = "name";
    private static final String defaultParamName = "default";
    private Value<String> name;
    private Value<Integer> defaultValue;

    private static java.util.Map<String, Integer> integerArguments = new TreeMap<>();


    public ArgI(@ParameterInfo(name = nameParamName, description = "the name of the integer argument.") Value<String> name,
                @ParameterInfo(name = defaultParamName, description = "the default value.", optional = true) Value<Integer> defaultValue) {

        this.name = name;
        if (name == null) throw new IllegalArgumentException("The mean value can't be null!");
        this.defaultValue = defaultValue;
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
        return new TreeMap<>() {{
            put(nameParamName, name);
            put(defaultParamName, defaultValue);
        }};
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