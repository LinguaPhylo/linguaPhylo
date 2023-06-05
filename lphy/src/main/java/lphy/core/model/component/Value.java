package lphy.core.model.component;

import lphy.core.model.GraphicalModelNodeVisitor;
import lphy.core.model.datatype.DoubleValue;
import lphy.core.narrative.Narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Value<T> implements GraphicalModelNode<T> {

    T value;
    private String id;
    List<ValueListener> listeners = new ArrayList<>();
    List<GraphicalModelNode> outputs = new ArrayList<>();

    // the function that produced this value, or null if this value was initialized another way;
    DeterministicFunction<T> function = null;

    // for UI
    boolean isClamped = false;

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

    public String getNarrative(boolean unique, Narrative narrative) {
        if (getGenerator() != null) {
            return getGenerator().getInferenceNarrative(this, unique, narrative);
        } else {
            if (!isAnonymous()) return toString();
            return "";
        }
    }

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

    public boolean isClamped() {
        return isClamped;
    }

    public void setClamped(boolean clamped) {
        isClamped = clamped;
    }

    public static final Value<Double> Double_1 = new DoubleValue(null, 1.0, null);
}
