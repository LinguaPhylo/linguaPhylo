package lphy.core;

import lphy.graphicalModel.Value;

import java.util.Map;
import java.util.Objects;

/**
 * Created by adru001 on 10/03/20.
 */
public class Arguments {

    Map<String, Value<?>> arguments;

    public Arguments(Map<String, Value<?>> arguments) {
        this.arguments = arguments;
    }

    public boolean getBoolean(String name, Object defaultValue) {
        Value<?> val = arguments.get(name);
        if (val == null) return (Boolean)defaultValue;
        return (Boolean)val.value();
    }

    public int getInteger(String name, Object defaultValue) {
        Value<?> val = arguments.get(name);
        if (val == null) return (Integer)defaultValue;
        return (Integer) val.value();
    }

    public double getDouble(String name, Object defaultValue) {
        Value<?> val = arguments.get(name);
        if (val == null) return (Double)defaultValue;
        return (Double) val.value();
    }

    public String getString(String name, Object defaultValue) {
        Value<?> val = arguments.get(name);
        if (val == null) return defaultValue.toString();
        return (String) val.value();
    }
}
