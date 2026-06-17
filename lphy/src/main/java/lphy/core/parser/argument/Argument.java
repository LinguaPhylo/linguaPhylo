package lphy.core.parser.argument;

import lphy.core.model.annotation.ParameterInfo;

import java.util.Set;

public class Argument implements Comparable<Argument> {

    public final int index;
    public final String name;
    /** deprecated former names accepted in place of {@link #name}, see {@link ParameterInfo#aliases()} */
    public final String[] aliases;
    public final String description;
    public final boolean optional;
    public final Class type;

    public Argument(int index, ParameterInfo parameterInfo, Class type) {
        this.index = index;
        this.name = parameterInfo.name();
        this.aliases = parameterInfo.aliases();
        this.description = parameterInfo.description();
        this.optional = parameterInfo.optional();
        this.type = type;
    }

    /**
     * @param argKeys the argument names supplied in a script
     * @return the key in {@code argKeys} that supplies this argument: the canonical {@link #name}
     * if present, otherwise the first matching {@link #aliases alias}, otherwise null.
     */
    public String matchingKey(Set<String> argKeys) {
        if (argKeys.contains(name)) return name;
        for (String alias : aliases) {
            if (argKeys.contains(alias)) return alias;
        }
        return null;
    }

    public int compareTo(Argument a) {
        if (a.index != index) return Integer.compare(index, a.index);
        return name.compareTo(a.name);
    }

    public final String setMethodName() {
        return setMethodName(name);
    }

    public static String setMethodName(String name) {
        return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public final String getMethodName() {
        return "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public String toString() {
        return "argument " + name + " index=" + index + " type=" + type + " description=" + description + " optional=" + optional;
    }
}
