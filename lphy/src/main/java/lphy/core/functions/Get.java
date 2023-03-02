package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.util.LoggerUtils;

/**
 * @author Walter Xie
 */
public class Get<V> extends DeterministicFunction<V> {

    public static final String KEY = "key";
    public static final String MAP = "map";

    public Get(@ParameterInfo(name = KEY, description = "the key of the map as String") Value<String> keyVal,
               @ParameterInfo(name = MAP, description = "the map") Value<java.util.Map<String, V>> mapVal) {
        setParam(KEY, keyVal);
        setParam(MAP, mapVal);
    }

    @GeneratorInfo(name="get", verbClause = "is the value from",
            description = "Get the value from a map given a string ID as the key.")
    public Value<V> apply() {
        String key = getKey().value();
        java.util.Map<String, V> map = getMap().value();
        if (!map.containsKey(key))
            LoggerUtils.log.severe("The map (" + getMap().getId() + ") does not contain the key (" +
                    key + "), all keys = " + map.keySet());

        V val = map.get(key);
        if (val instanceof Value<?> value)
            return new Value<>(value.getId(), (V) value.value(), this);
        return new Value<>(null, val, this);
    }

    public Value<String> getKey() {
        return (Value<String>)paramMap.get(KEY);
    }

    public Value<java.util.Map<String, V>> getMap() {
        return (Value<java.util.Map<String, V>>) paramMap.get(MAP);
    }

}
