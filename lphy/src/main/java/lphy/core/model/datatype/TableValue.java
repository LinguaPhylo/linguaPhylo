package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;

import java.util.List;
import java.util.Map;

/**
 * Used by ReadDelim
 */
public class TableValue extends Value<Map<String, List>> {

    public TableValue(String id, Map<String, List> value) {

        super(id, value);
    }

    /**
     * Constructs an anonymous Double value.
     *
     * @param value
     */
    public TableValue(Map<String, List> value) {

        super(null, value);
    }


    public TableValue(String id, Map<String, List> value, DeterministicFunction<Map<String, List>> function) {

        super(id, value, function);
    }


    public TableValue(Map<String, List> value, DeterministicFunction<Map<String, List>> function) {

        super(null, value, function);
    }

    @Override
    public String toString() {
        if (isAnonymous()) return getGenerator().codeString();
        return getId() + " = " + getGenerator().codeString();
    }

}
