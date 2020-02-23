package james.graphicalModel;

import james.app.DoubleArrayLabel;
import james.app.HasComponentView;

import javax.swing.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by adru001 on 18/12/19.
 */
public class Value<T> implements GraphicalModelNode<T>, Viewable {

    private T value;
    String id;
    List<ValueListener> listeners = new ArrayList<>();
    List<GraphicalModelNode> outputs = new ArrayList<>();

    // the function that produced this value, or null if this value was initialized another way;
    DeterministicFunction<T> function = null;

    public Value(String id, T value) {
        this.id = id;
        this.value = value;
    }

    public Value(String id, T value, DeterministicFunction<T> function) {
        this(id, value);
        this.function = function;
    }

    public final T value() {
        return value;
    }

    public String getLabel() {
        if (isAnonymous()) {
            return "[" + ((Parameterized)getOutputs().get(0)).getParamName(this) + "]";
        } else return getId();
    }

    public String codeString() {
        StringBuilder builder = new StringBuilder();
        if (function != null) {
            if (!isAnonymous()) builder.append(id + " = ");
            builder.append(function.codeString());
        } else {
            builder.append(toString());
        }
        return builder.toString();
    }

    public String toString() {
        if (isAnonymous()) return value.toString();
        return id + " = " + value;
    }

    public void setValue(T value) {
        this.value = value;
        for (ValueListener listener : listeners) {
            listener.valueSet();
        }
    }

    public JComponent getViewer() {
        if (value instanceof HasComponentView) {
            JComponent component = ((HasComponentView<T>) value).getComponent(this);
            return component;
        }

        if (value() instanceof Double[]) {
            return new DoubleArrayLabel((Value<Double[]>)this);
        }

        if (value.toString().length() < 130) {
            return new JLabel(value.toString());
        } else {
            String valueString = value.toString();
            valueString = valueString.replace(", ", ",\n");

            JTextArea textArea = new JTextArea(valueString);
            textArea.setEditable(false);

            return textArea;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeterministicFunction<T> getFunction() {
        return function;
    }

    public void addValueListener(ValueListener listener) {
        listeners.add(listener);
    }

    public static void traverseGraphicalModel(Value value, GraphicalModelNodeVisitor visitor, boolean post) {
        if (!post) visitor.visitValue(value);

        if (value instanceof RandomVariable) {
            traverseGraphicalModel(((RandomVariable) value).getGenerativeDistribution(), visitor, post);
        } else if (value.getFunction() != null) {
            traverseGraphicalModel(value.getFunction(), visitor, post);
        }
        if (post) visitor.visitValue(value);
    }

    private static void traverseGraphicalModel(DeterministicFunction function, GraphicalModelNodeVisitor visitor, boolean post) {
        if (!post) visitor.visitFunction(function);

        Map<String, Value> map = function.getParams();

        for (Map.Entry<String, Value> e : map.entrySet()) {
            traverseGraphicalModel(e.getValue(), visitor, post);
        }
        if (post) visitor.visitFunction(function);
    }

    public static void traverseGraphicalModel(GenerativeDistribution genDist, GraphicalModelNodeVisitor visitor, boolean post) {
        if (!post) visitor.visitGenDist(genDist);

        Map<String, Value> map = genDist.getParams();

        for (Map.Entry<String, Value> e : map.entrySet()) {
            traverseGraphicalModel(e.getValue(), visitor, post);
        }
        if (post) visitor.visitGenDist(genDist);
    }

    public void addOutput(Parameterized p) {
        outputs.add(p);
    }

    public void removeOutput(Parameterized p) {
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
    
}
