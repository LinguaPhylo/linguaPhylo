package lphy.base.functions;

import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.Value;
//lphy.parser.functions.MapValue
@Deprecated
public class Map<K,V> extends DeterministicFunction<java.util.Map<K, V>> {

    final String keysParamName;
    final String valuesParamName;

    public Map(@ParameterInfo(name = "keys", description = "the keys of the map as an array") Value<K[]> keys,
               @ParameterInfo(name = "values", description = "the values of the map as an array") Value<V[]> values) {
        keysParamName = getParamName(0);
        valuesParamName = getParamName(1);
        setParam(keysParamName, keys);
        setParam(valuesParamName, values);
    }

    @GeneratorInfo(name="map",description = "A map defined by parallel arrays of keys and values")
    public Value<java.util.Map<K, V>> apply() {
        Value<K[]> keys = (Value<K[]>)getParams().get(keysParamName);
        Value<V[]> values = (Value<V[]>)getParams().get(valuesParamName);

        K[] k = keys.value();
        V[] v = values.value();

        java.util.Map<K, V> map = new java.util.HashMap<>();
        for (int i = 0; i < k.length; i++) {
            map.put(k[i], v[i]);
        }
        return new Value<>(null, map, this);
    }
}
