package lphy.core.logger;

import lphy.core.model.Symbols;
import lphy.core.model.Value;

public interface ValueFormatter<T> {

    enum Mode {
        // The value of each replicate should be logged into separate containers (e.g. file).
        VALUE_PER_FILE,
        // The value of each replicate should be logged into separate lines within a single container.
        // Two values cannot go to the same line.
        VALUE_PER_LINE,
        // The value of each replicate should be logged into separate rows as a column in a single container.
        VALUE_PER_CELL
    }

    default String getExtension() {
        return ".log";
    }

    default Mode getMode() {
        return Mode.VALUE_PER_CELL;
    }

    Class<T> getDataTypeClass();

    default String header() {
        return getValueID();
    }

    /**
     * @param value  It is from {@link lphy.core.model.Value#value()}
     * @return default toString().
     */
    default String format(T value) {
        return value.toString();
    }

    default String footer() {
        return "";
    }

    // overwrite to return "", if no row name.
    default String getRowName(int rowId) {
        return String.valueOf(rowId);
    }

    String getValueID();

    // Factory method
    default ValueFormatter<T> create(Value<T> value) {
        return new ValueFormatter.Base<>(value.getId(), value.value());
    }

    class Base<T> implements ValueFormatter<T> {

        T value;
        String valueID;
//        public Base() { // for getDeclaredConstructor().newInstance()
//        }

        public Base(String valueID, T value) {
            this.valueID = Symbols.getCanonical(valueID);
            this.value = value;
        }

        @Override
        public Class<T> getDataTypeClass() {
            // getTypeParameters()[0] retrieves the type T
            return (Class<T>) getClass().getTypeParameters()[0].getClass();
        }

        @Override
        public String getValueID() {
            return valueID;
        }

    }

//    class IntegerValueFormatter extends ValueFormatter.Base {
//        public IntegerValueFormatter() {
//        }
//
//        @Override
//        public Class<?> getDataTypeClass() {
//            return Integer.class;
//        }
//    }

}
