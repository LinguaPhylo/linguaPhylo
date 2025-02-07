package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;

import java.util.Map;

/**
 * Used by {@link lphy.core.simulator.Simulate}
 */
public class MapValue extends Value<Map<String, Object>> {

    public MapValue(String id, Map<String, Object> value) {

        super(id, value);
    }

    /**
     * Constructs an anonymous Double value.
     *
     * @param value
     */
    public MapValue(Map<String, Object> value) {

        super(null, value);
    }


    public MapValue(String id, Map<String, Object> value, DeterministicFunction<Map<String, Object>> function) {

        super(id, value, function);
    }

    /**
     * Constructs an anonymous Double value and records the function that it was generated by
     *
     * @param value
     * @param function
     */
    public MapValue(Map<String, Object> value, DeterministicFunction<Map<String, Object>> function) {

        super(null, value, function);
    }

    @Override
    public String toString() {
        if (isAnonymous()) return getGenerator().codeString();
        return getId() + " = " + getGenerator().codeString();
    }


//    @MethodInfo(description = "get the map value given the key.",
//            category = GeneratorCategory.TAXA_ALIGNMENT,
//            examples = {"simulation/jcCoal.lphy"})
//    public Object get(String key) {
//        Map<String, Object> map = this.value();
//        if (!map.containsKey(key))
//            LoggerUtils.log.severe("The map (" + getId() + ") does not contain the key (" +
//                    key + "), all keys = " + map.keySet());
//        return map.get(key);
//    }

}
