package lphy.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The basic concept of LPhy.
 * It wraps the value to enable dynamic typing in the LPhy language.
 * The value is generated from a generator, but it can be null,
 * such as constants.
 * The input is its generator. The output(s) will be the place(s) where it is used,
 * e.g. argument value of other generators.
 *
 * @see RandomVariable
 * @see Generator
 */
public class Value<T> implements GraphicalModelNode<T> {

    protected T value;
    /**
     * it will be anonymous, if id is null or empty string. {@link #isAnonymous()}
     */
    private String id;
    List<ValueListener> listeners = new ArrayList<>();
    /**
     * the place(s) where it is used, e.g. argument value of other generators.
     */
    List<GraphicalModelNode> outputs = new ArrayList<>();
    /**
     * the function (generator) that produced this value,
     * or null if this value was initialized another way.
     * It is also the input.
     */
    DeterministicFunction<T> function = null;

    // for UI
    boolean isObserved = false;

    /**
     * Detect if the value has been changed, especially in GUI.
     * Reset to false, after {@link lphy.core.simulator.Sampler#sample(Long)} done.
     */
    private boolean isDirty = false; //TODO to dev

    public Value(String id, T value) {
        this.id = id;
        this.value = value;
    }

    public Value(String id, T value, DeterministicFunction<T> function) {
        this(id, value);
        this.function = function;
    }

    public final Class getType() {
        return value.getClass();
    }

    /**
     * Constructs an anonymous value.
     *
     * @param value
     * @param function
     */
    public Value(T value, DeterministicFunction<T> function) {
        this(null, value);
        this.function = function;
    }

    public final T value() {
        return value;
    }

    /**
     * Utility method to unwrap the primitive value from Primitive,
     * when the value is a Primitive
     *
//     * @param value   T
     * @return   If the value is Primitive, then unwrap the primitive value from Primitive,
     *           otherwise return the value
     */
//    public static final Object getPrimitive(Object value) {
//        if (value instanceof Primitive<?> primitive) {
//            if (value.getClass().equals(primitive.getPrimitiveType())) {
//                return primitive.getPrimitive();
//            } else
//                throw new RuntimeException("Primitive type mismatch ! Expected " + value.getClass() +
//                        " but got " + primitive.getPrimitiveType());
//        }
//        return value;
//    }

    public String getLabel() {
        if (isAnonymous()) {
            if (getOutputs().size() > 0) {
//                return "[" + ((Generator) getOutputs().get(0)).getParamName(this) + "]";
                // https://github.com/LinguaPhylo/linguaPhylo/issues/249
                return ((Generator) getOutputs().get(0)).getParamName(this);
            } else return "anonymous";
        } else return getId();
    }

    public String codeString() {
        StringBuilder builder = new StringBuilder();
        Generator generator = getGenerator();
        if (generator != null) {
            if (!isAnonymous()) {
                builder.append(id);
                builder.append(" ");
                builder.append(generator.generatorCodeChar());
                builder.append(" ");
            }
            builder.append(generator.codeString());
        } else {
            builder.append(toString());
        }
        return builder.toString();
    }

//    public String getNarrative(boolean unique, Narrative narrative) {
//        if (getGenerator() != null) {
//            return getGenerator().getInferenceNarrative(this, unique, narrative);
//        } else {
//            if (!isAnonymous()) return toString();
//            return "";
//        }
//    }

    public String toString() {
        if (isAnonymous()) return valueToString();
        return id + " = " + valueToString();
    }

    public String valueToString() {

        return ValueUtils.valueToString(value);
    }

    public void setValue(T value) {
        T oldValue = this.value;
        this.value = value;
        for (ValueListener listener : listeners) {
            listener.valueSet(oldValue, value);
        }
    }

    /**
     * @return a unique id for this value for internal purposes.
     */
    public String getUniqueId() {
        if (!isAnonymous()) return getId();
        return hashCode() + "";
    }

    public String getId() {
        return id;
    }

    public final String getCanonicalId() {
        if (!isAnonymous()) return Symbols.getCanonical(getId());
        return null;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the function that produced this value, or null if this value was initialized another way
     */
    public Generator<T> getGenerator() {
        return function;
    }

    /**
     * @return true if this value is a random variable, or the function of a random value.
     */
    public boolean isRandom() {
        return (this instanceof RandomVariable) || (function != null && function.hasRandomParameters());
    }

    public void addValueListener(ValueListener listener) {
        listeners.add(listener);
    }

    public void addOutput(Generator p) {
        if (!outputs.contains(p)) outputs.add(p);
    }

    public void removeOutput(Generator p) {
        outputs.remove(p);
    }

    public List<GraphicalModelNode> getOutputs() {
        return outputs;
    }

    @Override
    public List<GraphicalModelNode> getInputs() {
        if (function != null) return Collections.singletonList(function);
        return new ArrayList<>();
    }

    public boolean isAnonymous() {
        return id == null || id.equals("");
    }

    public boolean isConstant() {
        return !(this instanceof RandomVariable) && getGenerator() == null;
    }

    public void setFunction(DeterministicFunction f) {
        this.function = f;
    }

    public boolean isObserved() {
        return isObserved;
    }

    public void setObserved(boolean observed) {
        isObserved = observed;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }
}
