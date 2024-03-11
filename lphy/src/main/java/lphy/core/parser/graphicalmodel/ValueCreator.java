package lphy.core.parser.graphicalmodel;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Generator;
import lphy.core.model.Value;
import lphy.core.model.datatype.*;

import java.util.List;
import java.util.Map;

public class ValueCreator {

    public static final Value<Double> Double_1 = new DoubleValue(null, 1.0, null);


    public static Value createValue(Integer intValue, DeterministicFunction generator) {
        return new IntegerValue(null, intValue, generator);
    }

    public static Value createValue(Integer[] intArrayValue, DeterministicFunction generator) {
        return new IntegerArrayValue(null, intArrayValue, generator);
    }

    public static Value createValue(Double doubleValue, DeterministicFunction generator) {
        return new DoubleValue(null, doubleValue, generator);
    }

    public static Value createValue(Double[] doubleArrayValue, DeterministicFunction generator) {
        return new DoubleArrayValue(null, doubleArrayValue, generator);
    }

    public static Value createValue(Boolean booleanValue, DeterministicFunction generator) {
        return new BooleanValue(null, booleanValue, generator);
    }

    public static Value createValue(Boolean[] booleanArrayValue, DeterministicFunction generator) {
        return new BooleanArrayValue(null, booleanArrayValue, generator);
    }

    public static Value createValue(String strValue, DeterministicFunction generator) {
        return new StringValue(null, strValue, generator);
    }

    public static Value createValue(String[] strArrayValue, DeterministicFunction generator) {
        return new StringArrayValue(null, strArrayValue, generator);
    }

    public static Value createValue(Object value, DeterministicFunction generator) {
        if (value instanceof Integer) return createValue((Integer) value, generator);
        if (value instanceof Integer[]) return createValue((Integer[]) value, generator);
        if (value instanceof Double) return createValue((Double) value, generator);
        if (value instanceof Double[]) return createValue((Double[]) value, generator);
        if (value instanceof Boolean) return createValue((Boolean) value, generator);
        if (value instanceof Boolean[]) return createValue((Boolean[]) value, generator);
        if (value instanceof String) return createValue((String) value, generator);
        if (value instanceof String[]) return createValue((String[]) value, generator);
        // this will allow method call to return a List<T>
        if (value instanceof List) return createValue((List) value, generator);
        return new Value(null, value, generator);
    }

    /**
     * This is used to handle generic array, which has to be initiated as Object[].
     * @param arr2List   generic type
     * @param generator  a {@link DeterministicFunction}
     * @return           an array value created from a generic type list.
     * @param <T>        Integer, Double, Boolean, ...
     */
    public static <T> Value<T[]> createValue(List<T> arr2List, DeterministicFunction generator) {
        if (arr2List.get(0) instanceof Integer)
            return createValue(arr2List.toArray(Integer[]::new), generator);
        else if (arr2List.get(0) instanceof Double)
            return createValue(arr2List.toArray(Double[]::new), generator);
        else if (arr2List.get(0) instanceof Boolean)
            return createValue(arr2List.toArray(Boolean[]::new), generator);
        else if (arr2List.get(0) instanceof String)
            return createValue(arr2List.toArray(String[]::new), generator);
        return createValue(arr2List.toArray(), generator);
    }

    public static void traverseGraphicalModel(Value value, GraphicalModelNodeVisitor visitor, boolean post) {
        if (!post) visitor.visitValue(value);

        if (value.getGenerator() != null) {
            traverseGraphicalModel(value.getGenerator(), visitor, post);
        }
        if (post) visitor.visitValue(value);
    }

    private static void traverseGraphicalModel(Generator generator, GraphicalModelNodeVisitor visitor, boolean post) {
        if (!post) visitor.visitGenerator(generator);

        Map<String, Value> map = generator.getParams();

        for (Map.Entry<String, Value> e : map.entrySet()) {
            traverseGraphicalModel(e.getValue(), visitor, post);
        }
        if (post) visitor.visitGenerator(generator);
    }
}
