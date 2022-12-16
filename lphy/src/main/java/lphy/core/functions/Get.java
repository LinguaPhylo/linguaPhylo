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

    final String key;
    final java.util.Map<String, V> map;

    public Get(@ParameterInfo(name = "key", description = "the key of the map as String") Value<String> keyVal,
               @ParameterInfo(name = "map", description = "the map") Value<java.util.Map<String, V>> mapVal) {
        key = keyVal.value();
        map = mapVal.value();
        if (!map.containsKey(key))
            LoggerUtils.log.severe("The map (" + mapVal.getId() + ") does not contain the key (" +
                    key + "), all keys = " + map.keySet());
    }

    @GeneratorInfo(name="get",description = "Get the value from a map given a string ID as the key.")
    public Value<V> apply() {
        V val = map.get(key);
        return new Value<>(null, val, this);
    }
}
