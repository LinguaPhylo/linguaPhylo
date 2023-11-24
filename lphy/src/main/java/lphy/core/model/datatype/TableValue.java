package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;

/**
 * Used by ReadDelim
 */
public class TableValue extends Value<Table> {

    public TableValue(String id, Table value) {

        super(id, value);
    }

    /**
     * Constructs an anonymous Double value.
     *
     * @param value
     */
    public TableValue(Table value) {

        super(null, value);
    }


    public TableValue(String id, Table value, DeterministicFunction<Table> function) {

        super(id, value, function);
    }


    public TableValue(Table value, DeterministicFunction<Table> function) {

        super(null, value, function);
    }


    @Override
    public String toString() {
        if (isAnonymous()) return getGenerator().codeString();
        return getId() + " = " + getGenerator().codeString();
    }

}
