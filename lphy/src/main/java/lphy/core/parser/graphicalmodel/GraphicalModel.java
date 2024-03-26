package lphy.core.parser.graphicalmodel;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.DataClampingUtils;

import java.util.*;

public interface GraphicalModel {

    enum Context {
        data,
        model
    }

    /**
     * @return the data dictionary of values with id's, keyed by id
     */
    Map<String, Value<?>> getDataDictionary();

    /**
     * @return the model dictionary of values with id's, keyed by id
     */
    Map<String, Value<?>> getModelDictionary();

    Set<Value> getDataValues();

    Set<Value> getModelValues();

    /**
     * @return the value with the given id in the given context, or null if the value id doesn't exist in given context.
     */
    default Value getValue(String id, Context context) {
        switch (context) {
            case data:
                return getDataDictionary().get(id);
            case model:
            default:
                Map<String, Value<?>> data = getDataDictionary();
                Map<String, Value<?>> model = getModelDictionary();
                if (model.containsKey(id)) return model.get(id);
                return data.get(id);
        }
    }

    default boolean hasValue(String id, Context context) {
        return getValue(id, context) != null;
    }

    /**
     * @param id a value id
     * @return true if this id is contained in both the data block
     * and the model block and the model id is a random variable.
     */
    default boolean isClamped(String id) {
        return DataClampingUtils.isClamped(id, this);
    }

    /**
     * @param value  given a {@link Value}
     * @return  true if value is {@link RandomVariable}, which should be in model block,
     *          and it is clamped.
     */
    default boolean isClampedVariable(Value value) {
        return value instanceof RandomVariable && isClamped(value.getId());
    }

    /**
     * @return all sinks of the graphical model, including in the data block.
     */
    default List<Value<?>> getModelSinks() {
        List<Value<?>> nonArguments = new ArrayList<>();
        getDataDictionary().values().forEach((val) -> {
            if (!val.isAnonymous() && val.getOutputs().size() == 0) nonArguments.add(val);
        });
        getModelDictionary().values().forEach((val) -> {
            if (!val.isAnonymous() && val.getOutputs().size() == 0) nonArguments.add(val);
        });

        nonArguments.sort(Comparator.comparing(Value::getId));

        return nonArguments;
    }

    /**
     * @return a list of all random variables reachable (i.e. that are depended on by) the sinks.
     */
    default List<RandomVariable<?>> getAllVariablesFromSinks() {
        List<RandomVariable<?>> variables = new ArrayList<>();
        for (Value<?> value : GraphicalModelUtils.getAllValuesFromSinks(this)) {
            if (value instanceof RandomVariable<?>) {
                variables.add((RandomVariable<?>) value);
            }
        }
        return variables;
    }

    /**
     * @param value
     * @return true if this is a named value in the data block.
     */
    default boolean isNamedDataValue(Value value) {
        return (!value.isAnonymous() && !(value instanceof RandomVariable) && hasValue(value.getId(), Context.data));
    }

    default boolean inDataBlock(Value value) {
        return getDataValues().contains(value);
    }

    default double computeLogPosterior() {
        List<RandomVariable<?>> variables = this.getAllVariablesFromSinks();

        double logPosterior = 0.0;
        for (RandomVariable variable : variables) {

            if (!isClampedVariable(variable)) {
                logPosterior += variable.getGenerativeDistribution().logDensity(variable.value());
            } else {
                logPosterior += variable.getGenerativeDistribution().logDensity(getDataDictionary().get(variable.getId()));
            }
        }
        return logPosterior;
    }

    default void put(String id, Value value, Context context) {
        switch (context) {
            case data:
                getDataDictionary().put(id, value);
                getDataValues().add(value);
                break;
            case model:
            default:
                getModelDictionary().put(id, value);
                getModelValues().add(value);
        }
    }

}
