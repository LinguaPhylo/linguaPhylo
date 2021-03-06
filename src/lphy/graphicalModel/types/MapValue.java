package lphy.graphicalModel.types;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Value;

import java.util.Map;

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
}
