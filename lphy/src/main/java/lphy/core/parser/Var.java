package lphy.core.parser;

import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.datatype.BooleanArrayValue;
import lphy.core.model.datatype.IntegerArrayValue;
import lphy.core.model.datatype.RealArrayValue;
import lphy.core.model.datatype.StringArrayValue;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import lphy.core.vectorization.operation.ElementsAt;
import lphy.core.vectorization.operation.Range;
import lphy.core.vectorization.operation.RangeList;
import lphy.core.vectorization.operation.SliceDoubleArray;
import org.phylospec.types.Bool;
import org.phylospec.types.Int;
import org.phylospec.types.Real;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.max;

/**
 * A container for the Var parsing context in the parser, and associated methods
 */
public class Var {

    String id;
    RangeList rangeList;
    GraphicalModel graphicalModel;

    public Var(String id, GraphicalModel graphicalModel) {
        this(id, null, graphicalModel);
    }

    public Var(String id, RangeList rangeList, GraphicalModel graphicalModel) {
        this.id = id;
        this.graphicalModel = graphicalModel;
        this.rangeList = rangeList;
    }

    public boolean isRangedVar() {
        return rangeList != null;
    }

    public Value getValue(GraphicalModel.Context context) {

        Value val = graphicalModel.getValue(id, context);

        if (!isRangedVar()) {
            return val;
        } {
            return getIndexedValue(val, rangeList).apply();
        }
    }

    /**
     * @return a Slice or ElementsAt function
     */
    public static DeterministicFunction getIndexedValue(Value array, RangeList rangeList) {

        if (array.value() instanceof Real[]) {
            if (rangeList.isRange()) {
                Range range = (Range) rangeList.getRangeElement(0);
                return new SliceDoubleArray(range.start(), range.end(), array);
            }

            if (rangeList.isSingle()) {
                Value<Integer> i = (Value<Integer>) rangeList.getRangeElement(0);
                return new SliceDoubleArray(i, i, array);
            }
        }

        Value<Int[]> indices = rangeList.apply();

        return new ElementsAt(indices, array);
    }

    /**
     * Assign the given value to this var, and put the result in the graphical model context provided.
     * @param value the value to assign to this var
     * @param function the deterministic function that produce the value to assign, or null if no such function exists.
     * @param context the context in which this assignment is taking place
     * @return a new value that is the result of this assignment.
     */
    public Value assign(Value value, DeterministicFunction function, GraphicalModel.Context context) {

        if (!isRangedVar()) {
            if (function != null) value.setFunction(function);
            value.setId(id);
            graphicalModel.put(id, value, context);
            return value;
        }

        Int[] intArr = rangeList.apply().value();
        List<Integer> range = Arrays.stream(intArr).map(Int::getPrimitive).toList();

        // get max index
        int max = max(range);

        // if value already exists
        if (graphicalModel.hasValue(id, context)) {
            Value v = graphicalModel.getValue(id, context);

            // TODO how to handle double arrays?

            // TODO if the value already exists then it now has two functional parents? Need to add a second parent?

            // Generic array support for all types of single dimension arrays
            if (v.value().getClass().isArray()) {
                int currentLength = Array.getLength(v.value());

                if (currentLength <= max) {
                    // need to enlarge array
                    Object newArray = Array.newInstance(v.value().getClass().getComponentType(), max + 1);

                    for (int i = 0; i < currentLength; i++) {
                        Array.set(newArray, i, Array.get(v.value(), i));
                    }
                    v.setValue(newArray);
                }

                Object source = value.value();
                Object destinationArray = v.value();

                for (int i = 0; i < range.size(); i++) {
                    int index = range.get(i);
                    if (source.getClass().isArray()) {
                        Array.set(destinationArray, index, Array.get(source, i));
                    } else {
                        Array.set(destinationArray, index, source);
                    }
                }
            }
            return v;
        } else {
            // if this is a new value to be constructed
            // generic support for array creation
            if (value.value().getClass().isArray()) {

                //TODO

                Object sourceArray = value.value();

                Object destinationArray = Array.newInstance(sourceArray.getClass().getComponentType(), max + 1);
                for (int i = 0; i < range.size(); i++) {
                    int index = range.get(i);

                    Array.set(destinationArray, index, Array.get(sourceArray, i));
                }
                Value v;
                if (destinationArray instanceof Real[]) {
                    v = new RealArrayValue(id, (Real[]) destinationArray, function);
                } else if (destinationArray instanceof Int[]) {
                    v = new IntegerArrayValue(id, (Int[]) destinationArray, function);
                } else if (destinationArray instanceof Bool[]) {
                    v = new BooleanArrayValue(id, (Bool[]) destinationArray, function);
                } else if (destinationArray instanceof String[]) {
                    v = new StringArrayValue(id, (String[]) destinationArray, function);
                } else {
                    v = new Value(id, destinationArray, function);
                }

                LoggerUtils.log.fine("   adding value " + v + " to the dictionary");
                return v;
            } else {
                // handle singleton index
                Object sourceValue = value.value();

                Object destinationArray = Array.newInstance(sourceValue.getClass(), max + 1);
                for (int i = 0; i < range.size(); i++) {
                    int index = range.get(i);
                    Array.set(destinationArray, index, sourceValue);
                }
                Value v = null;
                if (destinationArray instanceof Real[]) {
                    v = new RealArrayValue(id, (Real[]) destinationArray, function);
                } else if (destinationArray instanceof Integer[]) {
                    v = new IntegerArrayValue(id, (Int[]) destinationArray, function);
                } else if (destinationArray instanceof Boolean[]) {
                    v = new BooleanArrayValue(id, (Bool[]) destinationArray, function);
                } else if (destinationArray instanceof String[]) {
                    v = new StringArrayValue(id, (String[]) destinationArray, function);
                } else {
                    v = new Value(id, destinationArray, function);
                }
                graphicalModel.put(id, v, context);
                return v;
            }
        }
    }

    public String getId() {
        return id;
    }

    public RangeList getRangeList() {
        return rangeList;
    }
}
